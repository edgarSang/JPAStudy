package mono;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class FruitBox {
    public static void main(String[] args) {
        final List<String> basket1 = Arrays.asList(new String[]{"kiwi", "orange", "lemon", "orange", "lemon", "kiwi"});
        final List<String> basket2 = Arrays.asList(new String[]{"banana", "lemon", "lemon", "kiwi"});
        final List<String> basket3 = Arrays.asList(new String[]{"strawberry", "orange", "lemon", "grape", "strawberry"});
        final List<List<String>> baskets = Arrays.asList(basket1, basket2, basket3);
        final Flux<List<String>> basketFlux = Flux.fromIterable(baskets);

        divideAndCount(basket1);
    }

    private static void divideAndCount(List<String> basket) {
        final Mono<List<String>> distinctFruits = Flux.fromIterable(basket).distinct().collectList();
        final Mono<Map<String,Long>> countFruitsMono = Flux.fromIterable(basket)
                .groupBy(fruit -> fruit) // 바구니로 부터 넘어온 과일 기준으로 group을 묶는다.
                .concatMap(groupedFlux -> groupedFlux.count()
                   .map(count -> {
                        final Map<String, Long> fruitCount = new LinkedHashMap<String, Long>();
                        fruitCount.put(groupedFlux.key(), count);
                        return fruitCount;
                    })
                )//concat Map 으로 순서보장
                .reduce((accmulateMap, currentMap) -> new LinkedHashMap<String, Long>(){{
                    putAll(accmulateMap);
                    putAll(currentMap);
                }});
        // 그동안 누적된 accumulatedMap에 현재 넘어오는 currentMap을 합쳐서 새로운 Map을 만든다.
        // map끼리 putAll하여 하나의 Map으로 만든다.
    }
}
