import {useCallback, useEffect, useMemo, useRef, useState} from 'react';
import {useDebounce} from '@/hooks/useDebounce';
import {spellsApi, type SpellSortField} from '@/services/api';
import {useCharacter} from '@/context/CharacterContext';
import {Modal} from '@/components/common/Modal';
import {Input, Select} from '@/components/common/Input';
import {Button} from '@/components/common/Button';
import type {SpellResponse} from '@/types';
import styles from './ManageSpellsModal.module.css';
import {SpellInfoModal} from "@components/character/SpellInfoModal.tsx";

interface ManageSpellsModalProps {
  isOpen: boolean;
  onClose: () => void;
}

interface SortOption {
  value: string;
  label: string;
  field: SpellSortField;
  order: 'asc' | 'desc';
}

const SORT_OPTIONS: SortOption[] = [
  {value: 'name-asc', label: 'Name (A → Z)', field: 'name', order: 'asc'},
  {value: 'name-desc', label: 'Name (Z → A)', field: 'name', order: 'desc'},
  {value: 'level-asc', label: 'Level (low → high)', field: 'level', order: 'asc'},
  {value: 'level-desc', label: 'Level (high → low)', field: 'level', order: 'desc'},
  {value: 'castingTime-asc', label: 'Casting Time', field: 'castingTime', order: 'asc'},
  {value: 'school-asc', label: 'School', field: 'school', order: 'asc'},
  {value: 'concentration-desc', label: 'Concentration', field: 'concentration', order: 'desc'},
  {value: 'ritual-desc', label: 'Ritual', field: 'ritual', order: 'desc'},
];

const PAGE_SIZE = 50;

const humanize = (raw: string): string =>
    raw.toLowerCase().replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase());

function buildSortParams(opt: SortOption): string[] {
  if (opt.field === 'name') return [`name,${opt.order}`];
  return [`${opt.field},${opt.order}`, 'name,asc'];
}

function groupKeyFor(spell: SpellResponse, field: SpellSortField): string | null {
  switch (field) {
    case 'level':
      return spell.level === 0 ? 'Cantrips' : `Level ${spell.level}`;
    case 'school':
      return humanize(spell.school);
    case 'castingTime':
      return spell.castingTime || 'Unknown Casting Time';
    case 'concentration':
      return spell.concentration ? 'Concentration' : 'No Concentration';
    case 'ritual':
      return spell.ritual ? 'Ritual' : 'Not a Ritual';
    case 'name':
    default:
      return null;
  }
}

export function ManageSpellsModal({isOpen, onClose}: ManageSpellsModalProps) {
  const {currentCharacter, addSpellToCharacter, saving} = useCharacter();

  const [searchQuery, setSearchQuery] = useState('');
  const [sortValue, setSortValue] = useState<string>(SORT_OPTIONS[0].value);

  const [spells, setSpells] = useState<SpellResponse[]>([]);
  const [page, setPage] = useState(0);
  const [isLast, setIsLast] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [hasLoadedOnce, setHasLoadedOnce] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [spellToView, setSpellToView] = useState<SpellResponse | null>(null);

  const debouncedSearch = useDebounce(searchQuery, 300);

  const sortOption = useMemo(
      () => SORT_OPTIONS.find(o => o.value === sortValue) ?? SORT_OPTIONS[0],
      [sortValue],
  );

  const sortParams = useMemo(() => buildSortParams(sortOption), [sortOption]);

  const scrollRef = useRef<HTMLDivElement | null>(null);
  const sentinelRef = useRef<HTMLDivElement | null>(null);
  const requestIdRef = useRef(0);

  const fetchPage = useCallback(
      async (pageToLoad: number, replace: boolean) => {
        const myRequestId = ++requestIdRef.current;
        if (replace) setIsRefreshing(true);
        else setIsLoading(true);
        setError(null);
        try {
          const response = await spellsApi.list({
            page: pageToLoad,
            size: PAGE_SIZE,
            search: debouncedSearch.trim() || undefined,
            sort: sortParams,
          });
          if (myRequestId !== requestIdRef.current) return;
          setSpells(prev => (replace ? response.content : [...prev, ...response.content]));
          setPage(response.pageNumber);
          setIsLast(response.last);
          setHasLoadedOnce(true);
          if (replace && scrollRef.current) scrollRef.current.scrollTop = 0;
        } catch (err) {
          if (myRequestId !== requestIdRef.current) return;
          setError(err instanceof Error ? err.message : 'Failed to load spells.');
        } finally {
          if (myRequestId === requestIdRef.current) {
            setIsLoading(false);
            setIsRefreshing(false);
          }
        }
      },
      [debouncedSearch, sortParams],
  );

  useEffect(() => {
    if (!isOpen) return;
    fetchPage(0, true);
  }, [isOpen, debouncedSearch, sortParams, fetchPage]);

  useEffect(() => {
    if (!isOpen) return;
    const sentinel = sentinelRef.current;
    const root = scrollRef.current;
    if (!sentinel || !root) return;

    const observer = new IntersectionObserver(
        entries => {
          const entry = entries[0];
          if (entry.isIntersecting && !isLoading && !isRefreshing && !isLast) {
            fetchPage(page + 1, false);
          }
        },
        {root, rootMargin: '200px'},
    );
    observer.observe(sentinel);
    return () => observer.disconnect();
  }, [isOpen, isLoading, isRefreshing, isLast, page, fetchPage]);

  useEffect(() => {
    if (!isOpen) {
      setSearchQuery('');
      setSortValue(SORT_OPTIONS[0].value);
      setSpells([]);
      setPage(0);
      setIsLast(false);
      setError(null);
      setHasLoadedOnce(false);
      setIsLoading(false);
      setIsRefreshing(false);
      setSpellToView(null);
      requestIdRef.current++;
    }
  }, [isOpen]);

  const knownSpellIds = useMemo(
      () => new Set(currentCharacter?.spells.map(s => s.id) ?? []),
      [currentCharacter?.spells],
  );

  const renderedItems = useMemo(() => {
    const items: Array<
        | { kind: 'group'; key: string; label: string }
        | { kind: 'spell'; key: string; spell: SpellResponse }
    > = [];
    let lastGroup: string | null = null;
    for (const spell of spells) {
      const group = groupKeyFor(spell, sortOption.field);
      if (group !== null && group !== lastGroup) {
        items.push({kind: 'group', key: `g-${group}`, label: group});
        lastGroup = group;
      }
      items.push({kind: 'spell', key: `s-${spell.id}`, spell});
    }
    return items;
  }, [spells, sortOption.field]);

  const showEmptyState =
      hasLoadedOnce && !isRefreshing && !isLoading && spells.length === 0 && !error;

  return (
      <>
        <Modal isOpen={isOpen} onClose={onClose} title="Add Spells to Spellbook" size="large">
          <div className={styles.container}>
            <div className={styles.header}>
              <Input
                  placeholder="Search spells by name..."
                  value={searchQuery}
                  onChange={e => setSearchQuery(e.target.value)}
                  fullWidth
              />
              <Select
                  aria-label="Sort spells"
                  value={sortValue}
                  onChange={e => setSortValue(e.target.value)}
                  options={SORT_OPTIONS.map(o => ({value: o.value, label: o.label}))}
              />
            </div>

            <div className={styles.resultsWrapper}>
              {isRefreshing && (
                  <div className={styles.refreshBar} aria-hidden="true"/>
              )}
              <div
                  className={styles.resultsContainer}
                  ref={scrollRef}
                  data-stale={isRefreshing || undefined}
              >
                {error && <p className={styles.errorText}>{error}</p>}
                {showEmptyState && <p className={styles.emptyText}>No spells found.</p>}

                {renderedItems.map(item =>
                    item.kind === 'group' ? (
                        <div key={item.key} className={styles.groupLabel}>
                          {item.label}
                        </div>
                    ) : (
                        <div
                            key={item.key}
                            className={styles.spellRow}
                            role="button"
                            tabIndex={0}
                            onClick={() => setSpellToView(item.spell)}
                            onKeyDown={e => {
                              if (e.key === 'Enter' || e.key === ' ') {
                                e.preventDefault();
                                setSpellToView(item.spell);
                              }
                            }}
                        >
                          <div className={styles.spellInfo}>
                            <span className={styles.spellName}>{item.spell.name}</span>
                            <span className={styles.spellMeta}>
                                {item.spell.level === 0 ? 'Cantrip' : `Lvl ${item.spell.level}`}
                              {' · '}{humanize(item.spell.school)}
                              {' · '}{item.spell.castingTime}
                              {item.spell.concentration && ' · Concentration'}
                              {item.spell.ritual && ' · Ritual'}
                              </span>
                          </div>
                          <Button
                              size="small"
                              disabled={knownSpellIds.has(item.spell.id) || saving}
                              onClick={e => {
                                e.stopPropagation();
                                addSpellToCharacter(item.spell.id);
                              }}
                          >
                            {knownSpellIds.has(item.spell.id) ? 'Known' : 'Add'}
                          </Button>
                        </div>
                    ),
                )}

                <div ref={sentinelRef} className={styles.sentinel} aria-hidden="true"/>

                {isLoading && !isRefreshing && (
                    <p className={styles.loadingText}>Loading…</p>
                )}
                {!isLoading && !isRefreshing && isLast && spells.length > 0 && (
                    <p className={styles.endOfListText}>End of list</p>
                )}
              </div>
            </div>
          </div>
        </Modal>
        <SpellInfoModal
            isOpen={spellToView !== null}
            onClose={() => setSpellToView(null)}
            spell={spellToView}
        />
      </>
  );
}
