import React from 'react';
import { Pressable, ScrollView, StyleSheet, Text } from 'react-native';
import { Category, CategoryId } from '../types';
import { theme } from '../theme';

interface Props {
  categories: Category[];
  activeId: CategoryId;
  onSelect: (id: CategoryId) => void;
}

export function CategoryTabs({ categories, activeId, onSelect }: Props) {
  return (
    <ScrollView
      horizontal
      showsHorizontalScrollIndicator={false}
      contentContainerStyle={styles.container}
    >
      {categories.map((category) => {
        const active = category.id === activeId;
        return (
          <Pressable
            key={category.id}
            onPress={() => onSelect(category.id)}
            accessibilityRole="button"
            accessibilityState={{ selected: active }}
            style={[
              styles.tab,
              {
                backgroundColor: active ? category.color : theme.colors.surface,
                borderColor: category.color,
              },
            ]}
          >
            <Text
              style={[
                styles.label,
                { color: active ? theme.colors.background : theme.colors.text },
              ]}
            >
              {category.label}
            </Text>
          </Pressable>
        );
      })}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    paddingHorizontal: theme.spacing.md,
    paddingVertical: theme.spacing.sm,
    gap: theme.spacing.sm,
  },
  tab: {
    paddingVertical: theme.spacing.sm,
    paddingHorizontal: theme.spacing.md,
    borderRadius: theme.radius.lg,
    borderWidth: 2,
    marginRight: theme.spacing.sm,
  },
  label: {
    fontSize: 15,
    fontWeight: '700',
  },
});
