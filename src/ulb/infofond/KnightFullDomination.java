package ulb.infofond;

import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hboulahy on 21/05/17.
 */
public class KnightFullDomination {

    private Model model;
    private BoolVar[] knightVars;
    private IntVar totalOfKnights;
    private boolean[][] knightAttacks;
    private int n;

    public KnightFullDomination(int n){
        this.n = n;
        this.model = new Model("KnightFullDomination");
        this.knightVars = model.boolVarArray("KnightsVars", getNumberOfElements());
        this.totalOfKnights = model.intVar("Total of Knights", 0, getNumberOfElements());
        this.knightAttacks = AttacksFactory.knightAttacks(n, n);
        postDominationConstraints();
        postTotalOfKnightsConstraint();
        model.setObjective(Model.MINIMIZE, totalOfKnights);
        Solver solver = model.getSolver();
        while(solver.solve()){
            solver.showShortStatistics();
            showMap();
        }
    }

    private void showMap() {
        for (int i = 0; i < getNumberOfRows(); i++) {
            for (int j = 0; j < getNumberOfCols(); j++) {
                int element = getElement(i, j);
                int knight = knightVars[element].getValue();
                String value = "*";
                if (knight == 1) {
                    value = "C";
                }
                System.out.print(String.format("%2s", value));
            }
            System.out.println();
        }
    }


    private void postTotalOfKnightsConstraint() {
        model.sum(knightVars, "=", totalOfKnights).post();
    }

    private void postDominationConstraints() {
        for (int i = 0; i < getNumberOfElements(); i++) {
            List<Constraint> constraintList = new ArrayList<>();
            for (int j = 0; j < getNumberOfElements(); j++) {
                if (i != j) {
                    Constraint dominationPerJ = model.or(model.arithm(knightVars[i], "=", 1),
                            Chess.buildAttackedByConstraint(model, knightVars, knightAttacks, i, j));
                    constraintList.add(dominationPerJ);
                }
            }
            Constraint[] constraints = new Constraint[constraintList.size()];
            constraintList.toArray(constraints);
            if(!constraintList.isEmpty()){
                model.or(constraints).post();
            }
        }
    }


    public int getNumberOfElements(){
        return getNumberOfRows() * getNumberOfCols();
    }

    public int getElement(int i, int j) {
        return i * getNumberOfCols() + j;
    }

    public int getNumberOfRows() {
        return n;
    }

    public int getNumberOfCols() {
        return n;
    }

    public static void main(String[] args){
        KnightFullDomination solver = new KnightFullDomination(2);
    }


}