package co.flock.controller;

import co.flock.db.Database;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.Pattern;

@org.springframework.stereotype.Controller

public class Controller {

    private static final String FILED_TEXT = "text";
    private static final Pattern SPACE_PATTERN = Pattern.compile("\\s+");
    private static final int MAX_TOP_SCORERS = 5;
    private Database _db;

    @RequestMapping(value = "/scorer", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> respond(@RequestParam("token") String token, @RequestBody Map<String, Object> req) {

        Map<String, String> map = new HashMap<>();
        String message = (String) req.get(FILED_TEXT);

        if (!StringUtils.isNotBlank(token))
            return null;

        _db = new Database(token);

        if (message != null) {
            message = message.trim();
            if (isScoreCheckerOrModifierMessage(message)) {
                String msgWithPrefixRemoved = message.substring(message.indexOf('@') + 1).trim();
                String[] splited = SPACE_PATTERN.split(msgWithPrefixRemoved);
                if (splited.length > 1) {
                    String last = splited[splited.length - 1];
                    boolean validInput;
                    Integer score = 0;
                    try {
                        score = Integer.valueOf(last);
                        validInput = true;
                    } catch (NumberFormatException ex) {
                        validInput = false;
                    }

                    if (validInput) {
                        String name = extractName(splited);
                        map.put(name, String.valueOf(putScore(name, score)));
                        return getResponseMap(map);
                    } else if (last.compareTo("++") == 0 || last.compareTo("--") == 0) {
                        boolean increment = last.compareTo("++") == 0;
                        String name = extractName(splited);
                        map.put(name, String.valueOf(modifyScore(name, increment)));
                        return getResponseMap(map);
                    } else {
                        String name = message.substring(message.indexOf('@') + 1).trim();
                        if (_db.containsKey(name)) {
                            map.put(name, String.valueOf(_db.get(name)));
                            return getResponseMap(map);
                        } else {
                            map.put(FILED_TEXT, "Not found!");
                            return map;
                        }
                    }
                }
            } else if (isLeaderboardFetcherMessage(message)) {
                Map<String, String> topScorers = getTopScorers(_db.getAll(), false, MAX_TOP_SCORERS);
                if (!topScorers.isEmpty()) {
                    return getResponseMap(topScorers);
                } else {
                    topScorers.put(FILED_TEXT, "No scores found!");
                    return topScorers;
                }
            }
        }

        return null;
    }

    private Map<String, String> getResponseMap(Map<String, String> map) {
        String responseText = getFormattedText(map);
        map.clear();
        map.put(FILED_TEXT, responseText);
        return map;
    }

    private static boolean isScoreCheckerOrModifierMessage(String message) {
        return StringUtils.containsIgnoreCase(message, "scorer") && message.contains("@");
    }

    private static String getFormattedText(Map<String, String> responseMap) {

        String response = "";

        for (Map.Entry<String, String> entry : responseMap.entrySet()) {
            response += entry.getKey() + " : " + entry.getValue() + '\n';
        }

        return response;
    }

    private static boolean isLeaderboardFetcherMessage(String message) {
        return StringUtils.containsIgnoreCase(message, "scorer") && StringUtils.containsIgnoreCase(message, "top");
    }

    private int modifyScore(String name, boolean increment) {

        if (_db.containsKey(name)) {
            Integer score = (Integer) _db.get(name);
            if (increment) {
                score++;
            } else {
                score--;
            }
            _db.put(name, score);
        } else {
            int score = 0;
            if (increment) {
                score++;
            } else {
                score--;
            }
            _db.put(name, score);
        }

        return (int) _db.get(name);
    }

    private int putScore(String name, int score) {
        if (_db.containsKey(name)) {
            Integer oldScore = (Integer) _db.get(name);
            int newScore = oldScore + score;
            _db.put(name, newScore);
        } else {
            _db.put(name, score);
        }
        return (int) _db.get(name);
    }

    private static String extractName(String[] message) {

        String name = "";

        for (int i = 0; i < message.length - 1; i++) {
            name = name + ' ' + message[i];
        }
        name = name.trim();
        return name;
    }

    private static Map<String, String> getTopScorers(Map<Object, Object> unsortMap, final boolean order, int max) {
        List<Map.Entry<Object, Object>> list = new LinkedList<>(unsortMap.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<Object, Object>>() {
            public int compare(Map.Entry<Object, Object> o1,
                               Map.Entry<Object, Object> o2) {
                if (order) {
                    return ((Integer) o1.getValue()).compareTo((Integer) o2.getValue());
                } else {
                    return ((Integer) o2.getValue()).compareTo((Integer) o1.getValue());

                }
            }
        });

        Map<String, String> sortedMap = new LinkedHashMap<>();
        int counter = 0;
        for (Map.Entry<Object, Object> entry : list) {
            sortedMap.put((String) entry.getKey(), String.valueOf(entry.getValue()));
            counter++;
            if (counter == max) {
                break;
            }
        }
        return sortedMap;
    }
}