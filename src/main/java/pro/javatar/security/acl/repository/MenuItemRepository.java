package pro.javatar.security.acl.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pro.javatar.security.acl.entity.MenuItem;

@Repository
public interface MenuItemRepository extends CrudRepository<MenuItem, Long> {
}
