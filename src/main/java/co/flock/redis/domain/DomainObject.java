package co.flock.redis.domain;

import java.io.Serializable;

public interface DomainObject extends Serializable {

    String getKey();

    String getObjectKey();
}

