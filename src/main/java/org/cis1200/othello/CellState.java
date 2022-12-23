package org.cis1200.othello;

public enum CellState {
    EMPTY, BLACK, WHITE;

    public CellState copy() {
        return switch (this) {
            case EMPTY -> EMPTY;
            case BLACK -> BLACK;
            case WHITE -> WHITE;
        };
    }
}
