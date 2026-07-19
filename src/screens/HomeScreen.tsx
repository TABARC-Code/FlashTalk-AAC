import React, { useMemo, useState } from 'react';
import { ScrollView, StyleSheet, Text, View } from 'react-native';
import { CategoryTabs } from '../components/CategoryTabs';
import { Tile } from '../components/Tile';
import { categories } from '../data/categories';
import { tiles } from '../data/tiles';
import { useSpeech } from '../hooks/useSpeech';
import { theme } from '../theme';
import { CategoryId, Tile as TileData } from '../types';

export function HomeScreen() {
  const [activeCategory, setActiveCategory] = useState<CategoryId>(categories[0].id);
  const { speak } = useSpeech();

  const activeColor = useMemo(
    () => categories.find((c) => c.id === activeCategory)?.color ?? theme.colors.accent,
    [activeCategory]
  );

  const visibleTiles = useMemo(
    () => tiles.filter((tile) => tile.category === activeCategory),
    [activeCategory]
  );

  const handleTilePress = (tile: TileData) => {
    speak(tile.phrase);
  };

  return (
    <View style={styles.screen}>
      <View style={styles.header}>
        <Text style={styles.title}>FlashTalk AAC</Text>
        <Text style={styles.subtitle}>Tap a tile to speak</Text>
      </View>

      <CategoryTabs
        categories={categories}
        activeId={activeCategory}
        onSelect={setActiveCategory}
      />

      <ScrollView contentContainerStyle={styles.grid}>
        {visibleTiles.map((tile) => (
          <Tile key={tile.id} tile={tile} color={activeColor} onPress={handleTilePress} />
        ))}
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  screen: {
    flex: 1,
    backgroundColor: theme.colors.background,
  },
  header: {
    paddingHorizontal: theme.spacing.md,
    paddingTop: theme.spacing.md,
    paddingBottom: theme.spacing.xs,
  },
  title: {
    color: theme.colors.text,
    fontSize: 26,
    fontWeight: '800',
  },
  subtitle: {
    color: theme.colors.textMuted,
    fontSize: 14,
    marginTop: 2,
  },
  grid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    padding: theme.spacing.sm,
    paddingBottom: theme.spacing.xl,
  },
});
