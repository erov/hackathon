import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.IntBinaryOperator;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Solver {
    private static final int INF = (int) 1e9 + 7;
    private static final DataHelper helper = DataHelper.INSTANCE;
    private static final ArrayList<RequestInform> requests = new ArrayList<>();
    private static final ArrayList<ArrayList<Integer>> months = new ArrayList<>();
    private static int[][] chill;
    private static  ArrayList<InputEdge>[] gr;
    private static int empCount;
    private static int nQuals;
    private static int nMonths;
    private static int t;

    private static boolean checkFlow(int[] flow, int id, int finalI) {
        boolean ok = true;
        for (int i = 0; i < nQuals; i++) {
            if (helper.getQualified().get(id).get(i) == 0) {
                continue;
            }
            if (flow[i] < helper.getRequiredPersonal().get(i).get(finalI)) {
                ok = false;
                break;
            }
        }
        return ok;
    }

    private static void setCapacity(int month, int v, int cap) {
        gr[month].get(v).setCap(cap);
    }

    private static void firstVacation() {
        for (int cur = 1; cur <= 12; cur++) {
            gr[cur] = new ArrayList<>();
            for (int i = 1; i <= nQuals; i++) {
                gr[cur].add(new InputEdge(i, t, 0));  // from quals to t
            }
            for (int i = 1; i <= empCount; i++) {
                gr[cur].add(new InputEdge(0, nQuals + i, 0));  // from s to emp
            }
            for (int i = 1; i <= empCount; i++) {
                for (int j = 1; j <= nQuals; j++) {
                    if (helper.isQualified(i - 1, j - 1)) {
                        gr[cur].add(new InputEdge(nQuals + i, j, INF));  // from emp to quals
                    }
                }
            }
        }

        ArrayList<Emp> emps = new ArrayList<>();
        boolean[] done = new boolean[empCount];
        for (int i = 0; i < empCount; i++) {
            emps.add(new Emp(helper.getStart().get(i).get(0), helper.getStartRight().get(i).get(0), i, helper.getQualified().get(i)));
            // {left start, right start, i, qualifications} ~ {sl, sr, id, q}
        }

        for (int l = 1; l <= 12; l++) {
            int finalI = l - 1;
            emps.sort(
                    Comparator.comparingInt((Emp a) -> helper.getStartRight().get(a.getId()).get(0))
                            .thenComparing((Emp a) -> helper.getRestPrior().get(a.getId()).get(finalI))
            );  // right start rise

            for (int i = 1; i <= empCount; i++) {
                setCapacity(l, nQuals + i - 1, helper.getMaxFly().get(i - 1).get(0));
            }
            for (int i = 1; i <= nQuals; i++) {
                setCapacity(l, i - 1, helper.getRequiredPersonal().get(i - 1).get(finalI));
            }

            FlowFinder flowFinder = new FlowFinder(gr[l], 0, t);
            int[] flow = flowFinder.maxFlow();  // qual -- worked hours

            for (Emp e : emps) {
                if (e.getSl() > l || done[e.getId()]) {
                    continue;
                }
                if (!checkFlow(flow, e.getId(), finalI)) {
                    continue;
                }
                setCapacity(l, nQuals + e.getId(), gr[l].get(nQuals + e.getId()).getCap() - helper.getParam("MIN_REST_SIZE"));

                FlowFinder trialFlowFinder = new FlowFinder(gr[l], 0, t);
                int[] trialFlow = trialFlowFinder.maxFlow();

                if (!checkFlow(trialFlow, e.getId(), finalI)) {
                    setCapacity(l, nQuals + e.getId(), gr[l].get(nQuals + e.getId()).getCap() + helper.getParam("MIN_REST_SIZE"));
                } else {
                    done[e.getId()] = true;
                    chill[l - 1][e.getId()] = helper.getParam("MIN_REST_SIZE");
                }
            }
        }
    }

    public static void otherVacation() {
        ArrayList<Integer> emps = new ArrayList<>();
        for (int i = 0; i < empCount; i++) {
            emps.add(i);
        }

        for (int l = 1; l <= 12; l++) {
            int finalL = l;

            emps.sort(Comparator.comparingInt(a -> helper.getRestPrior().get(a).get(finalL - 1)));

            int cntVacation = 0;
            for (int emp = 0; emp < empCount; emp++) {
                boolean ok = l >= helper.getStart().get(emps.get(emp)).get(0);
                for (int i = 0; i < 12; i++) {
                    cntVacation += (chill[i][emps.get(emp)] > 0 ? 1 : 0);
                }
                ok = ok && (cntVacation <= helper.getParam("TOTAL_RESTS"));

                for (int i = l; i > l - helper.getParam("MIN_REST_LAG") && i > 0; i--) {
                    if (chill[i - 1][emps.get(emp)] > 0) {
                        ok = false;
                        break;
                    }
                }

                int cnt = 1, sum = helper.getParam("MIN_REST_SIZE");
                if (helper.getMonth().get(l - 1).get(0) == 1) {
                    for (int m = 0; m < 12; m++) {
                        if (helper.getMonth().get(m).get(0) == 1 && chill[m][emps.get(emp)] > 0) {
                            cnt++;
                            sum += chill[m][emps.get(emp)];
                        }
                    }
                    ok = ok && (cnt <= helper.getParam("PRIOR_RESTS")) && (sum <= helper.getParam("REST_HIGH"));
                } else {
                    for (int m = 0; m < 12; m++) {
                        if (helper.getMonth().get(m).get(0) != 1 && chill[m][emps.get(emp)] > 0) {
                            cnt++;
                            sum += chill[m][emps.get(emp)];
                        }
                    }
                    ok = ok && (cnt <= helper.getParam("NOPRIOR_RESTS")) && (sum <= helper.getParam("REST_LOW"));
                }

                if (!ok) {
                    continue;
                }

                setCapacity(l, nQuals + emps.get(emp), gr[l].get(nQuals + emps.get(emp)).getCap() - helper.getParam("MIN_REST_SIZE"));

                FlowFinder flowFinder = new FlowFinder(gr[l], 0, t);
                int[] flow = flowFinder.maxFlow();  // qual -- worked hours

                int finalI = l - 1;
                if (checkFlow(flow, emps.get(emp), finalI)) {
                    chill[l - 1][emps.get(emp)] = helper.getParam("MIN_REST_SIZE");
                } else {
                    setCapacity(l, nQuals + emps.get(emp), gr[l].get(nQuals + emps.get(emp)).getCap() + helper.getParam("MIN_REST_SIZE"));
                }
            }
        }

    }

    public static void extendVacation() {
        ArrayList<Integer> emps = new ArrayList<>();
        for (int i = 0; i < empCount; i++) {
            emps.add(i);
        }

        emps.sort(Comparator.comparingInt(a -> -helper.qualificationSum(a)));

        for (int emp = 0; emp < empCount; emp++) {
            for (int l = 1; l <= 12; l++) {
                if (chill[l - 1][emps.get(emp)] > 0) {
                    int season = 0, totalYear = 0;
                    for (int m = 1; m <= 12; m++) {
                        if (helper.getMonth().get(l - 1).get(0) == helper.getMonth().get(m - 1).get(0)) {
                            season += chill[m - 1][emps.get(emp)];
                        }
                        totalYear += chill[m - 1][emps.get(emp)];
                    }

                    season = (helper.getMonth().get(l - 1).get(0) == 1 ? helper.getParam("REST_HIGH") : helper.getParam("REST_LOW")) - season;
                    int right = Math.min(season, helper.getMonth().get(l - 1).get(1) - chill[l - 1][emps.get(emp)]) + 1;
                    right = Math.min(right, helper.getMaxFly().get(emps.get(emp)).get(0) + 1);
                    right = Math.min(right, helper.getParam("REST_YEAR") - totalYear + 1);
                    right = Math.min(right, gr[l].get(nQuals + emps.get(emp)).getCap() + 1);
                    right = Math.max(right, 0);
                    int left = 0;

                    while (right - left > 1) {
                        int mid = (left + right) / 2;
                        setCapacity(l, nQuals + emps.get(emp), gr[l].get(nQuals + emps.get(emp)).getCap() - mid);
                        FlowFinder flowFinder = new FlowFinder(gr[l], 0, t);
                        int[] flow = flowFinder.maxFlow();  // qual -- worked hours
                        int finalI = l - 1;

                        if (checkFlow(flow, emps.get(emp), finalI)) {
                            left = mid;
                        } else {
                            right = mid;
                        }
                        setCapacity(l, nQuals + emps.get(emp), gr[l].get(nQuals + emps.get(emp)).getCap() + mid);
                    }
                    chill[l - 1][emps.get(emp)] += left;
                    setCapacity(l, nQuals + emps.get(emp), gr[l].get(nQuals + emps.get(emp)).getCap() - left);
                }
            }
        }
    }

    public static void init() {
        DataHelper.INSTANCE.init("input.xlsx");
        nMonths = helper.getParam("nMonths");
        empCount = helper.getQualified().size();
        nQuals = helper.getParam("nQuals");
        chill = new int[nMonths][empCount];
        gr = new ArrayList[nMonths + 1];
        t = 1 + empCount + nQuals;
    }

    public static void main(String[] args) {
        init();
        firstVacation();
        otherVacation();
        extendVacation();

//        int totalVacations = 0, sumVacations = 0;
//        for (int j = 0; j < empCount; j++) {
//            for (int i = 0; i < nMonths; i++) {
//                if (chill[i][j] > 0) {
//                    totalVacations++;
//                    sumVacations += chill[i][j];
//                }
//            }
//        }
//        System.out.println(totalVacations + " " + sumVacations);

        helper.out(chill, empCount);

    }

    static boolean check() {
        for (int i = 0; i < nMonths; i++) {
            for (int j = 0; j < nQuals; j++) {
                int def = helper.getRequiredPersonal().get(j).get(i);
                for (int t = 0; t < empCount; t++) {
                    if (helper.getQualified().get(t).get(j) == 0) {
                        continue;
                    }
                    def = def - helper.getMaxFly().get(t).get(0) + chill[i][t];
                }
                if (def > 0) return false;
            }
        }
        return true;
    }
}
