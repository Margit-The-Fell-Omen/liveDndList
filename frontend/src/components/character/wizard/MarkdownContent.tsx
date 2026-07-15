import styles from './MarkdownContent.module.css';

interface MarkdownContentProps {
  text: string;
}

type Block =
    | { kind: 'heading'; level: number; text: string }
    | { kind: 'table'; headers: string[]; rows: string[][] }
    | { kind: 'paragraph'; text: string };

function normalize(text: string): string {
  return text.replace(/\r\n/g, '\n').replace(/\r/g, '\n');
}

function splitCells(line: string): string[] {
  const trimmed = line.trim().replace(/^\|/, '').replace(/\|$/, '');
  return trimmed.split('|').map(c => c.trim());
}

function isTableSeparator(line: string): boolean {
  return /^\s*\|?\s*:?-{2,}:?\s*(\|\s*:?-{2,}:?\s*)+\|?\s*$/.test(line);
}

const HEADING_RE = /^(#{1,6})\s*(.*)$/;

function parseBlocks(source: string): Block[] {
  const lines = normalize(source).split('\n');
  const blocks: Block[] = [];
  let i = 0;

  while (i < lines.length) {
    const line = lines[i];

    if (!line.trim()) {
      i++;
      continue;
    }

    const headingMatch = HEADING_RE.exec(line);
    if (headingMatch) {
      blocks.push({
        kind: 'heading',
        level: headingMatch[1].length,
        text: headingMatch[2].trim(),
      });
      i++;
      continue;
    }

    if (line.trim().startsWith('|') && i + 1 < lines.length && isTableSeparator(lines[i + 1])) {
      const headers = splitCells(line);
      i += 2;
      const rows: string[][] = [];
      while (i < lines.length && lines[i].trim().startsWith('|')) {
        rows.push(splitCells(lines[i]));
        i++;
      }
      blocks.push({kind: 'table', headers, rows});
      continue;
    }

    const paragraphLines: string[] = [];
    while (
        i < lines.length &&
        lines[i].trim() &&
        !HEADING_RE.test(lines[i]) &&
        !(lines[i].trim().startsWith('|') && i + 1 < lines.length && isTableSeparator(lines[i + 1]))
        ) {
      paragraphLines.push(lines[i]);
      i++;
    }
    if (paragraphLines.length > 0) {
      blocks.push({kind: 'paragraph', text: paragraphLines.join(' ')});
    }
  }

  return blocks;
}

function renderInline(text: string): React.ReactNode[] {
  const parts: React.ReactNode[] = [];
  const regex = /(\*\*([^*]+)\*\*|\*([^*]+)\*|_([^_]+)_)/g;
  let lastIndex = 0;
  let match: RegExpExecArray | null;
  let key = 0;

  while ((match = regex.exec(text)) !== null) {
    if (match.index > lastIndex) {
      parts.push(text.slice(lastIndex, match.index));
    }
    const content = match[2] ?? match[3] ?? match[4] ?? '';
    if (match[2]) {
      parts.push(<strong key={key++}>{content}</strong>);
    } else {
      parts.push(<em key={key++}>{content}</em>);
    }
    lastIndex = regex.lastIndex;
  }
  if (lastIndex < text.length) {
    parts.push(text.slice(lastIndex));
  }
  return parts;
}

export function MarkdownContent({text}: MarkdownContentProps) {
  if (!text || !text.trim()) return null;

  const blocks = parseBlocks(text);

  return (
      <div className={styles.wrapper}>
        {blocks.map((block, idx) => {
          if (block.kind === 'heading') {
            const level = Math.min(Math.max(block.level, 3), 6);
            const Tag = `h${level}` as 'h3' | 'h4' | 'h5' | 'h6';
            return <Tag key={idx} className={styles.heading}>{renderInline(block.text)}</Tag>;
          }
          if (block.kind === 'table') {
            return (
                <div key={idx} className={styles.tableWrapper}>
                  <table className={styles.table}>
                    <thead>
                    <tr>
                      {block.headers.map((h, hIdx) => (
                          <th key={hIdx}>{renderInline(h)}</th>
                      ))}
                    </tr>
                    </thead>
                    <tbody>
                    {block.rows.map((row, rIdx) => (
                        <tr key={rIdx}>
                          {row.map((cell, cIdx) => (
                              <td key={cIdx}>{renderInline(cell)}</td>
                          ))}
                        </tr>
                    ))}
                    </tbody>
                  </table>
                </div>
            );
          }
          return (
              <p key={idx} className={styles.paragraph}>
                {renderInline(block.text)}
              </p>
          );
        })}
      </div>
  );
}
