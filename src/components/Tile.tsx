import React from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { Tile as TileData } from '../types';
import { theme } from '../theme';

interface Props {
  tile: TileData;
  color: string;
  onPress: (tile: TileData) => void;
}

export function Tile({ tile, color, onPress }: Props) {
  return (
    <Pressable
      onPress={() => onPress(tile)}
      accessibilityRole="button"
      accessibilityLabel={tile.label}
      hitSlop={4}
      style={({ pressed }) => [
        styles.tile,
        { borderColor: color, opacity: pressed ? 0.6 : 1 },
      ]}
    >
      <View style={[styles.emojiWrap, { backgroundColor: color }]}>
        <Text style={styles.emoji}>{tile.emoji}</Text>
      </View>
      <Text style={styles.label} numberOfLines={2}>
        {tile.label}
      </Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  tile: {
    flexBasis: '31%',
    minHeight: theme.tile.minHeight,
    backgroundColor: theme.colors.surface,
    borderRadius: theme.radius.md,
    borderWidth: 2,
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: theme.spacing.sm,
    paddingHorizontal: theme.spacing.xs,
    margin: theme.spacing.xs,
  },
  emojiWrap: {
    width: 44,
    height: 44,
    borderRadius: 22,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: theme.spacing.xs,
  },
  emoji: {
    fontSize: theme.tile.emojiSize,
  },
  label: {
    color: theme.colors.text,
    fontSize: theme.tile.fontSize,
    fontWeight: '600',
    textAlign: 'center',
  },
});
