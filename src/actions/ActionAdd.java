package actions;

import domain.Identifiable;
import repository.GenericRepositoryImpl;

public class ActionAdd<T extends Identifiable<Integer>> implements IAction {
    private GenericRepositoryImpl<T, Integer> repo;
    private T addedElem;

    public ActionAdd(GenericRepositoryImpl<T, Integer> repo, T addedElem) {
        this.repo = repo;
        this.addedElem = addedElem;
    }

    @Override
    public void executeUndo() {
        repo.delete(addedElem);
    }

    @Override
    public void executeRedo() {
        repo.add(addedElem);
    }
}
