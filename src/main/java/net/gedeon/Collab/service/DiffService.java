package net.gedeon.Collab.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * Service de calcul de différences entre textes
 *
 * Implémente l'algorithme de Myers pour calculer le diff entre deux textes
 */
@Service
public class DiffService {

    /**
     * Calcule le diff entre deux textes ligne par ligne
     */
    public String diff(String text1, String text2) {
        String[] lines1 = text1.split("\n");
        String[] lines2 = text2.split("\n");

        List<DiffLine> diffLines = computeDiff(lines1, lines2);
        return formatDiff(diffLines);
    }

    /**
     * Calcule le diff en utilisant l'algorithme de Myers
     */
    private List<DiffLine> computeDiff(String[] lines1, String[] lines2) {
        int n = lines1.length;
        int m = lines2.length;
        int max = n + m;

        int[] v = new int[2 * max + 1];
        int[][] trace = new int[max + 1][];

        // Algorithme de Myers
        for (int d = 0; d <= max; d++) {
            trace[d] = v.clone();

            for (int k = -d; k <= d; k += 2) {
                int x;
                if (k == -d || (k != d && v[k - 1 + max] < v[k + 1 + max])) {
                    x = v[k + 1 + max];
                } else {
                    x = v[k - 1 + max] + 1;
                }

                int y = x - k;

                while (x < n && y < m && lines1[x].equals(lines2[y])) {
                    x++;
                    y++;
                }

                v[k + max] = x;

                if (x >= n && y >= m) {
                    // Solution trouvée
                    return backtrack(lines1, lines2, trace, d);
                }
            }
        }

        // Pas de solution trouvée (ne devrait jamais arriver)
        return new ArrayList<>();
    }

    /**
     * Reconstitue le chemin de diff à partir de la trace
     */
    private List<DiffLine> backtrack(String[] lines1, String[] lines2, int[][] trace, int d) {
        List<DiffLine> result = new ArrayList<>();
        int x = lines1.length;
        int y = lines2.length;

        for (int depth = d; depth >= 0; depth--) {
            int[] v = trace[depth];
            int k = x - y;
            int max = lines1.length + lines2.length;

            int prevK;
            if (k == -depth || (k != depth && v[k - 1 + max] < v[k + 1 + max])) {
                prevK = k + 1;
            } else {
                prevK = k - 1;
            }

            int prevX = v[prevK + max];
            int prevY = prevX - prevK;

            while (x > prevX && y > prevY) {
                result.add(0, new DiffLine(DiffType.EQUAL, lines1[x - 1]));
                x--;
                y--;
            }

            if (depth > 0) {
                if (x == prevX) {
                    // Insertion
                    result.add(0, new DiffLine(DiffType.INSERT, lines2[y - 1]));
                } else {
                    // Suppression
                    result.add(0, new DiffLine(DiffType.DELETE, lines1[x - 1]));
                }
                x = prevX;
                y = prevY;
            }
        }

        return result;
    }

    /**
     * Formate le diff en texte lisible
     */
    private String formatDiff(List<DiffLine> diffLines) {
        StringBuilder sb = new StringBuilder();

        for (DiffLine line : diffLines) {
            switch (line.type) {
                case INSERT:
                    sb.append("+ ").append(line.content).append("\n");
                    break;
                case DELETE:
                    sb.append("- ").append(line.content).append("\n");
                    break;
                case EQUAL:
                    sb.append("  ").append(line.content).append("\n");
                    break;
            }
        }

        return sb.toString();
    }

    /**
     * Type de différence
     */
    private enum DiffType {
        INSERT, // Ligne ajoutée
        DELETE, // Ligne supprimée
        EQUAL // Ligne identique
    }

    /**
     * Représente une ligne du diff
     */
    private static class DiffLine {
        DiffType type;
        String content;

        DiffLine(DiffType type, String content) {
            this.type = type;
            this.content = content;
        }
    }
}
