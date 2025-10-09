import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

public class App {

    public static void n2_for_tree(ArrayList<ArrayList<Integer>> graph) {
        Queue<Integer> q = new ArrayDeque<>();
        int ans = 0;
        
        for (int i = 0; i < graph.size(); i++) {
            boolean[] visited = new boolean[graph.size()];
            q.add(i);
            visited[i] = true;  
            int count = -1;    
        
            int cur = 1;
            int prev = 0;
            
            while (!q.isEmpty()) {
                int u = q.poll();
                cur -= 1;
                
                for (int j = 0; j < graph.get(u).size(); j++) {
                    int v = graph.get(u).get(j);
                    if (!visited[v]) {
                        visited[v] = true;
                        q.add(v);
                        prev++;
                    }
                }
            
                if (cur == 0) {
                    cur = prev;
                    prev = 0;
                    count += 1;
                }
            }

            ans = Math.max(ans, count);
        }

        System.out.println("n2_for_tree " + ans);
    }

    public static void n_for_tree(ArrayList<ArrayList<Integer>> graph) {
        Queue<Integer> q = new ArrayDeque<>();
        boolean[] visited = new boolean[graph.size()];

        q.add(0);
        visited[0] = true;
        
        int u = -1;

        while(!q.isEmpty()) {
            u = q.poll();
            for (int j = 0; j < graph.get(u).size(); j++) {
                int v = graph.get(u).get(j);
                if (!visited[v]) {
                    visited[v] = true;
                    q.add(v);
                }
            }
        }

        int count = -1;
        int cur = 1;
        int prev = 0;
        q.add(u);
        visited = new boolean[graph.size()];
        visited[u] = true;
        while(!q.isEmpty()) {
            u = q.poll();
            cur -= 1;
            for (int j = 0; j < graph.get(u).size(); j++) {                    
                int v = graph.get(u).get(j);
                if (!visited[v]) {
                    visited[v] = true;
                    q.add(v);
                    prev++;
                }
            }
            if (cur == 0) {
                cur = prev;
                prev = 0;
                count += 1;
            }
        }

        System.out.println("n_for_tree " + count);

    }

    public static int[] deikstra(ArrayList<ArrayList<Integer>> graph, int from) {
        PriorityQueue<int[]> q = new PriorityQueue<>((a, b) -> a[1] - b[1]);
        int[] dist = new int[graph.size()];
        for (int i = 0; i < dist.length; i++) {
            dist[i] = -1;
        }
        dist[from] = 0;
        q.offer(new int[]{from, 0});

        while(!q.isEmpty()) {
            int[] pair = q.poll();
            int u = pair[0];
            int d = pair[1];

            if (d > dist[u]) {
                continue;
            }

            for (int j = 0; j < graph.get(u).size(); j++) {
                int v = graph.get(u).get(j);
                if ((dist[v] == -1) || (dist[v] > d + 1)) {
                    dist[v] = d + 1;
                    q.offer(new int[]{v, dist[v]});
                }
            }
        }

        return dist;   
    }

    public static void n2logn_for_graph(ArrayList<ArrayList<Integer>> graph) {
        int ans = -1;
        for (int i = 0; i < graph.size(); i++) {
            int[] z = deikstra(graph, i);
            for (int j = 0; j < z.length; j++) {
                ans = Math.max(ans, z[j]);
            }
        }
        System.out.println("n2logn_for_graph " + ans);

    }

    public static void n_for_one_cycle_graph(ArrayList<ArrayList<Integer>> graph) {
        ArrayList<Integer> cycle = new ArrayList<>();
        int[] from = new int[graph.size()];
        for (int i = 0; i< from.length; i++) {from[i] = -1;}
        boolean[] visited = new boolean[graph.size()];
        boolean foundCycle = false;

        for (int i = 0; i < graph.size() && !foundCycle; i++) {
            if (visited[i]) continue;
            
            Stack<int[]> st = new Stack<>();
            st.push(new int[]{i, -1});
            
            while (!st.empty() && !foundCycle) {
                int[] pair = st.pop();
                int u = pair[0];
                int parent = pair[1];

                visited[u] = true;
                from[u] = parent;

                for (int v : graph.get(u)) {
                    if (v == parent) continue;
                    
                    if (visited[v]) {
                        foundCycle = true;
                        cycle.add(v);
                        int cur = u;
                        while (cur != v) {
                            cycle.add(cur);
                            cur = from[cur];
                        }
                        break;
                    } else {
                        st.push(new int[]{v, u});
                    }
                }
            }
        }

        System.out.println("Cycle: " + cycle);
        if (cycle.isEmpty()) return;
        int n = cycle.size();
        int[] diams = new int[n];
        int[] depths = new int[n];

        if (n < 3) {
            System.out.println("HMMMMMMMMMMMMMM: CYCLE LESS THAN 3?????");
            return;
        }

        int maxDiametr = -1;

        int[] pair1 = find_depth_diam(graph, cycle.get(0),new int[]{cycle.get(1), cycle.get(n - 1)} );
        int[] pair2 = find_depth_diam(graph, cycle.get(n - 1), new int[]{cycle.get(0), cycle.get(n - 2)} );

        diams[0] = pair1[1];
        depths[0] = pair1[0];
        diams[n - 1] = pair2[1];
        depths[n - 1] = pair2[0];
 
        for (int i = 1; i < n - 1; i++) {
            int[] pair = find_depth_diam(graph, cycle.get(i), new int[]{cycle.get(i - 1), cycle.get(i + 1)});
            diams[i] = pair[1];
            depths[i] = pair[0];
        }

        for (int i = 0; i < diams.length; i++) {
            maxDiametr = Math.max(maxDiametr, diams[i]);
        }

        String s = "[";
        for (int i = 0; i < diams.length; i++) {
            s += depths[i];
            s += ", ";
        }
        
        int[] dcycle = new int[2 * n];
        int[] ddiams = new int[2 * n];
        int[] ddepths = new int[2 * n];
        for (int i = 0; i < n; i++) {
            dcycle[i] = dcycle[i + n] = cycle.get(i);
            ddiams[i] = ddiams[i + n] = diams[i];
            ddepths[i] = ddepths[i + n] = depths[i];
        }

        ArrayDeque<Integer> q = new ArrayDeque<>();

        for (int i = 0; i < 2 * n; i++) {
            while (!q.isEmpty() && (i - q.peekFirst()) > n / 2) {
                q.removeFirst();
            }

            if (!q.isEmpty()) {
                int j = q.peekFirst(); 
                int dist = i - j;
                int maybe_good = ddepths[i] + ddepths[j] + Math.min(dist, n - dist);
                maxDiametr = Math.max(maxDiametr, maybe_good);
            }
            while (!q.isEmpty() && (ddepths[q.peekLast()] - q.peekLast()<= ddepths[i] - i)) {
                q.removeLast();
            }
            q.add(i);
        }

        System.out.println("n_for_one_cycle_graph: " + maxDiametr);
    }
    
    private static int[] find_depth_diam(ArrayList<ArrayList<Integer>> graph, int from, int[] bad ) {

        Queue<Integer> q = new ArrayDeque<>();
        boolean[] visited = new boolean[graph.size()];

        q.add(from);
        visited[from] = true;
        
        int depth = -1;
        int u = -1;
        int cur = 1;
        int prev = 0;
        while(!q.isEmpty()) {
            u = q.poll();
            cur -= 1;
            for (int j = 0; j < graph.get(u).size(); j++) {
                int v = graph.get(u).get(j);
                if (!visited[v] && v != bad[0] && v != bad[1]) {
                    visited[v] = true;
                    q.add(v);
                    prev += 1;
                }
            }
            if (cur == 0) {
                cur = prev;
                prev = 0;
                depth+=1;
            }
        }

        int count = -1;
        cur = 1;
        prev = 0;
        q.add(u);
        visited = new boolean[graph.size()];
        visited[u] = true;
        while(!q.isEmpty()) {
            u = q.poll();
            cur -= 1;
            for (int j = 0; j < graph.get(u).size(); j++) {                    
                int v = graph.get(u).get(j);
                if (!visited[v] && v != bad[0] && v != bad[1]) {
                    visited[v] = true;
                    q.add(v);
                    prev++;
                }
            }
            if (cur == 0) {
                cur = prev;
                prev = 0;
                count += 1;
            }
        }
        return new int[]{depth, count};
    }

    public static void main(String[] args) throws Exception {
        ArrayList<ArrayList<Integer>> tree1 = new ArrayList<>();
        int n = 5;
        for (int i = 0; i < n; i++) {
            tree1.add(new ArrayList<>());
        }
        for (int i = 0; i < n - 1; i++) {
            tree1.get(i).add(i + 1);
            tree1.get(i + 1).add(i);
        }

        ArrayList<ArrayList<Integer>> tree2 = new ArrayList<>();
        n = 6;
        for (int i = 0; i < n; i++) {
            tree2.add(new ArrayList<>());
        }
        for (int i = 1; i < n; i++) {
            tree2.get(0).add(i);
            tree2.get(i).add(0);
        }
        
        
        System.out.println("0-1-2-3-4");
        n2_for_tree(tree1);
        n_for_tree(tree1);
        n2logn_for_graph(tree1);
        
        System.out.println("0-1\n -2\n -3\n -4\n -5");
        n2_for_tree(tree2);
        n_for_tree(tree2);
        n2logn_for_graph(tree2);

        ////////////////////////////////////////////////////////////////////
        ArrayList<ArrayList<Integer>> graph1 = new ArrayList<>();
        for (int i = 0; i < 3; i++) graph1.add(new ArrayList<>());
        graph1.get(0).add(1); graph1.get(1).add(0);
        graph1.get(1).add(2); graph1.get(2).add(1);
        graph1.get(2).add(0); graph1.get(0).add(2);

        ArrayList<ArrayList<Integer>> graph2 = new ArrayList<>();
        for (int i = 0; i < 5; i++) graph2.add(new ArrayList<>());
        graph2.get(0).add(1); graph2.get(1).add(0);
        graph2.get(1).add(2); graph2.get(2).add(1);
        graph2.get(2).add(3); graph2.get(3).add(2);
        graph2.get(3).add(0); graph2.get(0).add(3);
        graph2.get(1).add(4); graph2.get(4).add(1);

        ArrayList<ArrayList<Integer>> graph3 = new ArrayList<>();
        for (int i = 0; i < 6; i++) graph3.add(new ArrayList<>());
        graph3.get(2).add(3); graph3.get(3).add(2);
        graph3.get(3).add(4); graph3.get(4).add(3);
        graph3.get(4).add(2); graph3.get(2).add(4);


        n_for_one_cycle_graph(graph1);
        n_for_one_cycle_graph(graph2);
        n_for_one_cycle_graph(graph3);
        n_for_one_cycle_graph(tree2);

        
    }
}
