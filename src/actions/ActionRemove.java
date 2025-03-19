package actions;

import domain.Identifiable;
import repository.GenericRepositoryImpl;

public class ActionRemove<T extends Identifiable<Integer>> implements IAction {
    private GenericRepositoryImpl<T, Integer> repo;
    private T removedElem;

    public ActionRemove(GenericRepositoryImpl<T, Integer> repo, T removedElem) {
        this.repo = repo;
        this.removedElem = removedElem;
    }

    @Override
    public void executeUndo() {
        repo.add(removedElem);
    }

    @Override
    public void executeRedo() {
        repo.delete(removedElem);
    }
}
