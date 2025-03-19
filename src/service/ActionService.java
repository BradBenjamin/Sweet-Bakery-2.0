package service;

import actions.IAction;

import java.util.Stack;

public abstract class ActionService {
    protected Stack<IAction> undoStack = new Stack<>();
    protected Stack<IAction> redoStack = new Stack<>();

    public void undo() {
        if (!undoStack.isEmpty()) {
            IAction action = undoStack.pop();
            action.executeUndo();
            redoStack.push(action);
        } else {
            System.out.println("No actions to undo.");
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            IAction action = redoStack.pop();
            action.executeRedo();
            undoStack.push(action);
        } else {
            System.out.println("No actions to redo.");
        }
    }
}
