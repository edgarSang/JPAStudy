package mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FruitInfo {
    private List<String> distinctInfo;
    private Map<String,Long> countFruits;

    public FruitInfo(List<String> distinctInfo, Map<String, Long> countFruits) {
        this.distinctInfo = distinctInfo;
        this.countFruits = countFruits;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        FruitInfo fruitInfo = (FruitInfo) o;

        return Objects.equals(distinctInfo, fruitInfo.distinctInfo) && Objects.equals(
                countFruits, fruitInfo.countFruits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(distinctInfo, countFruits);
    }

    @Override
    public String toString() {
        return "FruitInfo{" +
               "distinctInfo=" + distinctInfo +
               ", countFruits=" + countFruits +
               '}';
    }
}
