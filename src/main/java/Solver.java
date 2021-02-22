import kotlin.Triple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class Solver {

    private static int INF = (int) 1e9 + 7;
    private static final DataHelper helper = DataHelper.INSTANCE;
    private static final ArrayList<RequestInform> requests = new ArrayList<>();
    private static final ArrayList<ArrayList<Integer>> months = new ArrayList<>();
    private static final int[][] chill = new int[12][5000];

    public static int priorityRequest(int p, int m) {
        return (4 - helper.getRestPrior().get(p).get(m)) * (helper.getMaxPersonalLevel() + 1)
                + helper.getPersonalLevel().get(p).get(1);
    }

    private static void firstVacation() {
        final int userCount = helper.getQualified().size();


        int sum = 0;
        for (int i = 0; i < userCount; i++) {
            if (helper.getQualified().get(i).get(0) == 1) {
                sum += helper.getMaxFly().get(i).get(0);
            }
        }
        System.out.println(sum);


        final int nQuals = helper.getParam("nQuals");
        ArrayList<InputEdge> gr = new ArrayList<>();
        final int t = 1 + userCount + nQuals;
        for (int i = 1; i <= nQuals; i++) {
            gr.add(new InputEdge(i, t, 0));
        }
        for (int i = 1; i <= userCount; i++) {
            gr.add(new InputEdge(0, nQuals + i, 0));
        }
        for (int i = 1; i <= userCount; i++) {
            for (int j = 1; j <= nQuals; j++) {
                if (helper.isQualified(i - 1, j - 1)) {
                    gr.add(new InputEdge(nQuals + i, j, INF));
                }
            }
        }
        ArrayList<Emp> emps = new ArrayList<>(0);
        boolean[] done = new boolean[userCount];
        for (int i = 0; i < userCount; i++) {
            emps.add(new Emp(helper.getStart().get(i).get(0), helper.getStartRight().get(i).get(0), i, helper.getQualified().get(i)));
            // {left start, right start, i, qualifications} ~ {sl, sr, id, q}
        }
        for (int l = 1; l <= 12; l++) { // current month
            int finalI = l - 1;
            emps.sort(
                    Comparator.comparingInt((Emp a) -> helper.getStartRight().get(a.getId()).get(0))
                            .thenComparing((Emp a) -> helper.getRestPrior().get(a.getId()).get(finalI))
            );  // right start rise
            for (int i = 1; i <= userCount; i++) {
                gr.get(nQuals + i - 1).setCap(helper.getMaxFly().get(i - 1).get(0));
            }
            for (int i = 1; i <= nQuals; i++) {
                gr.get(i - 1).setCap(helper.getRequiredPersonal().get(i - 1).get(finalI));
            }

            FlowFinder flowFinder = new FlowFinder(gr, 0, t);
            int[] flow = flowFinder.maxFlow();  // qual -- worked hours

            for (int i = 0; i < 10; i++) {
                System.out.print(flow[i] + " ");
            }
            System.out.println();

            for (Emp e : emps) {
                if (e.getSl() > l || done[e.getId()]) {
                    continue;
                }
                boolean v = true;
                for (int i = 0; i < nQuals; i++) {
                    if (helper.getQualified().get(e.getId()).get(i) == 0) {
                        continue;
                    }
                    if (flow[i] < helper.getRequiredPersonal().get(i).get(finalI)) {
                        v = false;
                        break;
                    }
                }
                if (!v) {
                    continue;
                }
                gr.get(nQuals + e.getId()).setCap(gr.get(nQuals + e.getId()).getCap() - helper.getParam("MIN_REST_SIZE"));
                FlowFinder trialFlowFinder = new FlowFinder(gr, 0, t);
                int[] trialFlow = trialFlowFinder.maxFlow();
                v = true;
                for (int i = 0; i < nQuals; i++) {
                    if (helper.getQualified().get(e.getId()).get(i) == 0) {
                        continue;
                    }
                    if (trialFlow[i] < helper.getRequiredPersonal().get(i).get(finalI)) {
                        v = false;
                        break;
                    }
                }
                if (!v) {
                    gr.get(nQuals + e.getId()).setCap(gr.get(nQuals + e.getId()).getCap() + helper.getParam("MIN_REST_SIZE"));
                } else {
                    done[e.getId()] = true;
                    chill[l - 1][e.getId()] = helper.getParam("MIN_REST_SIZE");
                }
            }
        }
    }

    public static void main(String[] args) {
        DataHelper.INSTANCE.init("input.xlsx");
        firstVacation();
        System.out.println("result: " + check());

//        int totalVacations = 0;
//        for (int j = 0; j < helper.getQualified().size(); j++) {
////                if (chill[i][j] > 0) {
////                    totalVacations++;
////                }
//            for (int i = 0; i < 12; i++) {
//                if (chill[i][j] > 0 && helper.getQualified().get(i).get(1) == 1) {
//                    System.out.println(j + " " + (i + 1) + " " + helper.getStart().get(j).get(0));
//                }
//            }
//        }
//
//        System.out.println(totalVacations);
    }

    static boolean check() {
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 10; j++) {
                int def = helper.getRequiredPersonal().get(j).get(i);
                for (int t = 0; t < 2427; t++) {
                    if (helper.getQualified().get(t).get(j) == 0) {
                        continue;
                    }
                    def = def - helper.getMaxFly().get(t).get(0) + chill[i][t];
                }
                if (def == 0) return false;
            }
        }
        return true;
    }
}
