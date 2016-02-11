package co.flock.controller;

import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.Pattern;

@org.springframework.stereotype.Controller

public class Controller {

    private static final String FILED_TEXT = "text";
    private static final Pattern SPACE_PATTERN = Pattern.compile("\\s+");
    private static final int MAX_TOP_SCORERS = 5;
    private final Map<String, Integer> _scoreMap = new HashMap<>();

    @RequestMapping(value = "/scorer", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> respond(@RequestBody Map<String, Object> req) {

        Map<String, String> responseMap = new HashMap<>();
        String message = (String) req.get(FILED_TEXT);
        if (message != null) {
            message = message.trim();
            if (isScorerCounterModificationMessage(message)) {
                String msgWithPrefixRemoved = message.substring(message.indexOf('@') + 1).trim();
                String[] splited = SPACE_PATTERN.split(msgWithPrefixRemoved);
                if (splited.length > 1) {
                    String last = splited[splited.length - 1];
                    if (last.compareTo("++") == 0) {
                        String name = extractName(splited);
                        responseMap.put(name, String.valueOf(incrementScore(name)));
                        return responseMap;
                    } else if (last.compareTo("--") == 0) {
                        String name = extractName(splited);
                        responseMap.put(name, String.valueOf(decrementScore(name)));
                        return responseMap;
                    }
                }
            } else if (isScorerLeaderBoardMessage(message)) {
                Map<String, String> topScorers = getTopScorers(_scoreMap, false, MAX_TOP_SCORERS);
                if (!topScorers.isEmpty()) {
                    return topScorers;
                }
            }
        }

        return null;
    }

    private static boolean isScorerLeaderBoardMessage(String message) {
        return message.contains("scorer") && message.contains("top");
    }

    private static boolean isScorerCounterModificationMessage(String message) {
        return message.contains("scorer") && message.contains("@") && (message.contains("++") || message.contains("--"));
    }

    private int decrementScore(String name) {
        if (_scoreMap.containsKey(name)) {
            Integer score = _scoreMap.get(name);
            score--;
            _scoreMap.put(name, score);
        } else {
            _scoreMap.put(name, -1);
        }

        return _scoreMap.get(name);
    }

    private int incrementScore(String name) {
        if (_scoreMap.containsKey(name)) {
            Integer score = _scoreMap.get(name);
            score++;
            _scoreMap.put(name, score);
        } else {
            _scoreMap.put(name, 1);
        }

        return _scoreMap.get(name);
    }

    private static String extractName(String[] message) {

        String name = "";

        for (int i = 0; i < message.length - 1; i++) {
            name = name + ' ' + message[i];
        }
        name = name.trim();
        return name;
    }

    private static Map<String, String> getTopScorers(Map<String, Integer> unsortMap, final boolean order, int max) {
        List<Map.Entry<String, Integer>> list = new LinkedList<>(unsortMap.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
                if (order) {
                    return o1.getValue().compareTo(o2.getValue());
                } else {
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });

        Map<String, String> sortedMap = new LinkedHashMap<>();
        int counter = 0;
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), String.valueOf(entry.getValue()));
            counter++;
            if (counter == max) {
                break;
            }
        }
        return sortedMap;
    }
}