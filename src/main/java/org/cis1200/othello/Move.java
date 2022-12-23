package org.cis1200.othello;

import java.util.Collection;

public record Move(int x, int y, Player player, Collection<int[]> flips) {

}
