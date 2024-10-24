package DAO;

import java.util.List;
import java.util.Optional;

public interface BaseDao<T> {
    Optional<T> getById(int id);

    List<T> getAll();

    T insert(T t);

    boolean update(T t);

    boolean delete(T t);
}