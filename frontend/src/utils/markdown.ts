export function stripMarkdown(text: string): string {
  if (!text) return '';
  return text
      .replace(/\r\n/g, '\n')
      .replace(/\r/g, '\n')
      .replace(/^#{1,6}\s*/gm, '')
      .replace(/\*\*([^*]+)\*\*/g, '$1')
      .replace(/\*([^*]+)\*/g, '$1')
      .replace(/_([^_]+)_/g, '$1')
      .replace(/^\s*\|.*\|\s*$/gm, '')
      .replace(/\n{2,}/g, ' — ')
      .replace(/\n/g, ' ')
      .replace(/\s+/g, ' ')
      .trim();
}