package co.flock.redis.repository;

import co.flock.redis.domain.DomainObject;

import java.util.List;

public interface Repository<V extends DomainObject> {

    void put(V obj);

    V get(V key);

    void delete(V key);

    List<V> getObjects();
}
