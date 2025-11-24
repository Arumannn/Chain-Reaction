package com.silent.treatment.chainreaction.strategy;

import com.silent.treatment.chainreaction.model.Board;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;

public interface ExplosionStrategy {
    void explode(Cell cell, Board board, Player player);
}