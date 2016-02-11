package co.flock.controller;

import co.flock.FlockMessagePoster;
import co.flock.redis.domain.User;
import co.flock.redis.repository.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.Pattern;

@org.springframework.stereotype.Controller

public class Controller {

    private static final String FILED_TEXT = "text";
    private static final Pattern SPACE_PATTERN = Pattern.compile("\\s+");
    private static final int MAX_TOP_SCORERS = 5;
    private UserRepository userRepository;

    @Autowired
    public void setRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

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
                        FlockMessagePoster.Post(getFormattedText(responseMap));
                        return responseMap;
                    } else if (last.compareTo("--") == 0) {
                        String name = extractName(splited);
                        responseMap.put(name, String.valueOf(decrementScore(name)));
                        FlockMessagePoster.Post(getFormattedText(responseMap));
                        return responseMap;
                    }
                }
            } else if (isScorerLeaderBoardMessage(message)) {
                Map<String, String> topScorers = getTopScorers(getScoreMap(), false, MAX_TOP_SCORERS);
                if (!topScorers.isEmpty()) {
                    FlockMessagePoster.Post(getFormattedText(topScorers));
                    return topScorers;
                } else {
                    FlockMessagePoster.Post("No scores!");
                }
            } else if (isScorerIndiScoreMessage(message)) {
                String name = message.substring(message.indexOf('@') + 1).trim();
                if (getScoreMap().containsKey(name)) {
                    responseMap.put(name, String.valueOf(getScoreMap().get(name)));
                    FlockMessagePoster.Post(getFormattedText(responseMap));
                    return responseMap;
                } else {
                    FlockMessagePoster.Post("Not found!");
                }
            }
        }

        return null;
    }

    private static boolean isScorerIndiScoreMessage(String message) {
        return StringUtils.containsIgnoreCase(message, "scorer") && message.contains("@");
    }

    private static String getFormattedText(Map<String, String> responseMap) {

        String response = "";

        for (Map.Entry<String, String> entry : responseMap.entrySet()) {
            response += entry.getKey() + " : " + entry.getValue() + '\n';
        }

        return response;
    }

    private static boolean isScorerLeaderBoardMessage(String message) {
        return StringUtils.containsIgnoreCase(message, "scorer") && StringUtils.containsIgnoreCase(message, "top");
    }

    private static boolean isScorerCounterModificationMessage(String message) {
        return StringUtils.containsIgnoreCase(message, "scorer") && message.contains("@") && (message.contains("++") || message.contains("--"));
    }

    private int decrementScore(String name) {
        Map<String, Integer> scoreMap = getScoreMap();
        if (scoreMap.containsKey(name)) {
            Integer score = scoreMap.get(name);
            score--;
            scoreMap.put(name, score);
            userRepository.put(new User(name,score));
        } else {
            userRepository.put(new User(name,-1));
            scoreMap.put(name, -1);
        }

        return scoreMap.get(name);
    }

    private int incrementScore(String name) {
        Map<String, Integer> scoreMap = getScoreMap();
        if (scoreMap.containsKey(name)) {
            Integer score = scoreMap.get(name);
            score++;
            userRepository.put(new User(name,score));
            scoreMap.put(name, score);
        } else {
            userRepository.put(new User(name,-1));
            scoreMap.put(name, 1);
        }

        return scoreMap.get(name);
    }

    private Map<String, Integer> getScoreMap() {
        List<User> users = userRepository.getObjects();
        Map<String, Integer> map = new HashMap<>();
        for (User user : users) {
            map.put(user.getName(), user.getScore());
        }
        return map;
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