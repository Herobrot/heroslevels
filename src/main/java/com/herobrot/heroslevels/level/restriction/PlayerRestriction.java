package com.herobrot.heroslevels.level.restriction;

import java.util.Map;

/**
 * @param skillLevelRestrictions skillId, lvl
 */
public record PlayerRestriction(int id, Map<Integer, Integer> skillLevelRestrictions) {}