import kotlin.Triple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class Solver {

    private static short writersAge = 5;
    private static int INF = (int)1e9 + 7;
    private static DataHelper helper = DataHelper.INSTANCE;
    private static ArrayList<RequestInform> requests = new ArrayList<>();
    private static ArrayList<ArrayList<Integer>> months = new ArrayList<>();

    public static int priorityRequest(int p, int m) {
        return (4 - helper.getRestPrior().get(p).get(m)) * (helper.getMaxPersonalLevel() + 1)
                + helper.getPersonalLevel().get(p).get(1);
    }

    private static void firstVacation(){
        int userCount = helper.getQualified().size();
        int nQuals = helper.getParam("nQuals");
        ArrayList<InputEdge> gr = new ArrayList<>();
        int t = 1 + userCount + nQuals;
        for (int i = 1; i <= nQuals; i++) {
            gr.add(new InputEdge(i, t, 0));
        }
        for (int i = 1; i <= userCount; i++) {
            gr.add(new InputEdge(0, nQuals + i, 0));
        }
        for (int i = 1; i <= userCount; i++) {
            for (int j = 1; j <= nQuals; j++) {
                if (helper.isQualified(i - 1, j - 1)) {
                    gr.add(new InputEdge(nQuals + i, i, INF));
                }
            }
        }

        ArrayList<Emp> emps = new ArrayList<>(userCount);
        ArrayList<Boolean> done = new ArrayList<>(userCount);
        for (int i = 0; i < userCount; i++) {
            emps.set(i, new Emp(helper.getStart().get(i).get(0), helper.getStartRight().get(i).get(0), i, helper.getQualified().get(i)));
        }
        for (int l = 1; l <= 12; l++) {
            int finalI = l - 1;
            emps.sort(
                    Comparator.comparingInt((Emp a) -> helper.getStartRight().get(a.getId()).get(0))
                            .thenComparing((Emp a) -> helper.getRestPrior().get(a.getId()).get(finalI))
            );
            for (int i = 1; i <= userCount; i++) {
                gr.get(nQuals + i - 1).setCap();
            }
            for (int i = 1; i <= nQuals; i++) {

            }
            for (Emp e: emps) {
                if (e.getSl() > l || done.get(e.getId())) {
                    continue;
                }

            }
        }
    }

    public static void main(String[] args) {
        DataHelper.INSTANCE.init("input.xlsx");

    }
}
