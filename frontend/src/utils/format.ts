export function displayName(title: string, nickname?: string) {
  return nickname?.trim() ? nickname : title;
}

export function ageLabel(ageRange: string) {
  const map: Record<string, string> = {
    AGE_0_2: '0-2岁',
    AGE_2_4: '2-4岁',
    AGE_4_6: '4-6岁'
  };
  return map[ageRange] || ageRange;
}
