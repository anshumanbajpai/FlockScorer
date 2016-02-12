package co.flock.db;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;

public class Database {

    private static final String FILE_PATH = "db.txt";
    private static final String SCORE_MAP = "scoreMap";
    private final DB _db;
    private final ConcurrentNavigableMap _treeMap;

    public Database() {
        _db = DBMaker.newFileDB(new File(FILE_PATH)).make();
        _treeMap = _db.getTreeMap(SCORE_MAP);
    }

    public Object get(Object key) {
        return _treeMap.get(key);
    }

    public void put(Object key, Object value) {
        _treeMap.put(key, value);
        _db.commit();
    }

    public Map<Object, Object> getAll() {

        Map<Object, Object> map = new HashMap<>();
        for (Object key : _treeMap.keySet()) {
            map.put(key, _treeMap.get(key));
        }
        return map;
    }

    public boolean containsKey(Object key) {
        return _treeMap.containsKey(key);
    }
}
