import kotlin.Triple;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;

import static java.lang.Integer.min;

public class FlowFinder {
    private int INF = (int) (1e9 + 7);
    private int MAXN = 510;
    private ArrayList<ArrayList<Integer>> g = new ArrayList<>(MAXN);
    private ArrayList<Edge> ed = new ArrayList<>(MAXN);
    private ArrayDeque<Integer> q = new ArrayDeque<>();
    private int[] dist = new int[MAXN];
    private int[] ptr = new int[MAXN];
    private int s, t;

    public FlowFinder (ArrayList<Triple<Integer, Integer, Integer>> edges, int s, int t) {
        for (Triple<Integer, Integer, Integer> edg: edges){
            addEdge(edg.component1(), edg.component2(), edg.component3(),0);
        }
    }

    void addEdge(int u, int v, int cap, int flow) {
        g.get(u).add(ed.size());
        ed.add(new Edge(v, ed.size(), cap, flow));
        g.get(v).add(ed.size());
        ed.add(new Edge(u, ed.size(), 0, -flow));
    }

    boolean bfs() {
        Arrays.fill(dist, INF);
        Arrays.fill(ptr, 0);
        dist[s] = 0;
        q.push(s);
        while (q.size() != 0) {
            int u = q.getFirst();
            q.pop();
            for (int id : g.get(u)) {
                if (ed.get(id).getCap() - ed.get(id).getFlow() > 0 && dist[ed.get(id).getTo()] == INF) {
                    dist[ed.get(id).getTo()] = dist[u] + 1;
                    q.push(ed.get(id).getTo());
                }
            }
        }
        return (dist[t] != INF);
    }

    int dfs(int u, int flow) {
        if (u == t || flow == 0) {
            return flow;
        }
        for (; ptr[u] < (int)g.get(u).size(); ++ptr[u]) {
            int id = g.get(u).get(ptr[u]);
            if (dist[ed.get(id).getTo()] == dist[u] + 1) {
                int pushed = dfs(ed.get(id).getTo(), min(flow, ed.get(id).getCap() - ed.get(id).getFlow()));
                if (pushed != 0) {
                    ed.get(id).setFlow(ed.get(id).getFlow() + pushed);
                    ed.get(id ^ 1).setFlow(ed.get(id ^ 1).getFlow() - pushed);
                    return pushed;
                }
            }
        }
        return 0;
    }

    ArrayList<Integer> maxFlow() {
        long flow = 0;
        while (bfs()) {
            int pushed = dfs(s, INF);
            do {
                flow += pushed;
                pushed = dfs(s, INF);
            } while (pushed != 0);
        }
        int n = g.get(t).size();
        ArrayList<Integer> ans = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            ans.set(ed.get(g.get(t).get(i)).getTo() - 1, ed.get(g.get(t).get(i)).getFlow());
        }
        return ans;
    }

}

