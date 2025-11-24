package com.silent.treatment.chainreaction.view;

import com.silent.treatment.chainreaction.model.Cell;

public interface GameObserver {
    void update(Cell cell);
}