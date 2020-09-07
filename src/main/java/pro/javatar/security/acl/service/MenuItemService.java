package pro.javatar.security.acl.service;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Permission;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.javatar.security.acl.AclManager;
import pro.javatar.security.acl.entity.MenuItem;
import pro.javatar.security.acl.repository.MenuItemRepository;

import java.util.List;

@Service
public class MenuItemService {

    private final MenuItemRepository repository;
    private final AclManager aclManager;

    @Autowired
    public MenuItemService(MenuItemRepository repository, AclManager aclManager) {
        this.repository = repository;
        this.aclManager = aclManager;
    }

    public Long create(MenuItem menu, String userId) {
        MenuItem created = repository.save(menu);
        Long createdId = created.getId();

        aclManager.addPermission(MenuItem.class, createdId, new PrincipalSid(userId), BasePermission.READ);
        aclManager.addPermission(MenuItem.class, createdId, new PrincipalSid(userId), BasePermission.DELETE);

        return createdId;
    }

    @PostFilter("hasPermission(filterObject, 'read')")
    public List<MenuItem> getMyMenuItems() {
        return Lists.newArrayList(repository.findAll());
    }

    public void delete(Long id, String userId) {
        if (hasPermission(id, userId, BasePermission.DELETE)) {
            repository.deleteById(id);
            return;
        }
        throw new AccessDeniedException("Current user does not have access to delete menu item with id " + id);
    }

    @Transactional
    public void grantPermission(Long id, String retriever, Permission permission) {
        // TODO: check rights: owner  or admin
        if (!hasPermission(id, retriever, permission))  {
            aclManager.grantPermission(MenuItem.class, id, new PrincipalSid(retriever), permission);
        }
    }

    public boolean hasPermission(Long id, String userId, Permission permission) {
        return aclManager.isPermissionGranted(MenuItem.class, id, new PrincipalSid(userId), permission);
    }

    public void removePermission(Long id, String userId, Permission permission) {
        aclManager.removePermission(MenuItem.class, id, new PrincipalSid(userId), permission);
    }
}
