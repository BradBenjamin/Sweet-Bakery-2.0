package actions;

import domain.Identifiable;
import repository.GenericRepositoryImpl;

public class ActionUpdate<T extends Identifiable<Integer>> implements IAction {
    private GenericRepositoryImpl<T, Integer> repo;
    private T oldElem;
    private T newElem;

    public ActionUpdate(GenericRepositoryImpl<T, Integer> repo, T oldElem, T newElem) {
        this.repo = repo;
        this.oldElem = oldElem;
        this.newElem = newElem;
    }

    @Override
    public void executeUndo() {
        repo.update(oldElem);
    }

    @Override
    public void executeRedo() {
        repo.update(newElem);
    }
}
