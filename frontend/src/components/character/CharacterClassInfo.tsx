import {useCharacter} from '@/context/CharacterContext';
import {ClassDetailPanel} from './wizard/ClassDetailPanel';
import styles from './CharacterClassInfo.module.css';

export function CharacterClassInfo() {
  const {currentCharacter, classes} = useCharacter();

  if (!currentCharacter) return null;

  return (
      <div className={styles.container}>
        {currentCharacter.classesInfo.map((classEntry) => {
          const classRef = classes.find(c => c.key === classEntry.classKey);

          if (!classRef) return null;

          return (
              <section key={classEntry.classKey} className={styles.classSection}>
                <header className={styles.classHeader}>
                  <h2 className={styles.className}>{classRef.name}</h2>
                  <span className={styles.classLevel}>Level {classEntry.level}</span>
                </header>

                <div className={styles.contentWrapper}>
                  <ClassDetailPanel
                      cls={classRef}
                      allClasses={classes}
                      currentLevel={classEntry.level}
                  />
                </div>
              </section>
          );
        })}
      </div>
  );
}