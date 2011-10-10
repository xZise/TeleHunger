package de.xzise.bukkit.telehunger;

import java.util.Comparator;

import org.matheclipse.parser.client.ast.ASTNode;
import org.matheclipse.parser.client.eval.DoubleEvaluator;

public class Algorithm {

    public static final Comparator<Algorithm> DISTANCE_COMPARATOR = new Comparator<Algorithm>() {
        public int compare(Algorithm o1, Algorithm o2) {
            if (o1.distance < 0) {
                if (o2.distance < 0) {
                    return 0;
                } else {
                    return -1;
                }
            } else if (o2.distance < 0) {
                return +1;
            } else {
                return Double.compare(o1.distance, o2.distance);
            }
        }
    };

    private final double distance;
    private final boolean applyAlways;
    private final DoubleEvaluator engine;
    private final ASTNode parsedFormula;
    private final String formula; 

    public Algorithm(final double distance, final boolean applyAlways, final DoubleEvaluator engine, final ASTNode parsedFormula, final String formula) {
        this.distance = distance;
        this.applyAlways = applyAlways;
        this.engine = engine;
        this.parsedFormula = parsedFormula;
        this.formula = formula;
    }

    public boolean matched(double distance) {
        if (this.distance < 0) {
            return true;
        } else {
            return this.distance >= distance;
        }
    }

    public boolean isApplingAlways() {
        return this.applyAlways;
    }

    public float calculateHunger() {
        return new Double(this.engine.evaluateNode(this.parsedFormula)).floatValue();
    }

    public boolean betterMatch(Algorithm other) {
        return other == null || (other.distance > this.distance && this.distance >= 0);
    }

    public String getFormula() {
        return this.formula;
    }
}
