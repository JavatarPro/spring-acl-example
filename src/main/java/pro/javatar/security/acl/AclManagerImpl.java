package pro.javatar.security.acl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.javatar.security.acl.service.MutableAclServiceImpl;

import java.io.Serializable;
import java.util.Arrays;

@Transactional
@Service
public class AclManagerImpl implements AclManager {

    private static final Logger log = LoggerFactory.getLogger(AclManagerImpl.class);

    @Autowired
    private MutableAclServiceImpl aclService;

    @Override
    public <T> void addPermission(Class<T> clazz, Serializable identifier, Sid sid, Permission permission) {
        ObjectIdentity identity = new ObjectIdentityImpl(clazz, identifier);
        aclService.createOrRetrieveSidPrimaryKey(sid);
        MutableAcl acl = isNewAcl(identity);
        isPermissionGranted(permission, sid, acl);
        aclService.updateAcl(acl);
        log.info("Permission {} was added for sid {}", permission, sid);
    }

    @Override
    public <T> void grantPermission(Class<T> clazz, Serializable identifier, Sid sid, Permission permission) {
        ObjectIdentity identity = new ObjectIdentityImpl(clazz, identifier);
        Long sidId = aclService.createOrRetrieveSidPrimaryKey(sid);
        MutableAcl acl = isNewAcl(identity);

        aclService.grantPermission(identity, sidId, permission, acl.getEntries().size());
        log.info("Permission {} was granted for user {}", permission, sidId);
    }

    @Override
    public <T> void removePermission(Class<T> clazz, Serializable identifier, Sid sid, Permission permission) {
        ObjectIdentity identity = new ObjectIdentityImpl(clazz.getCanonicalName(), identifier);
        MutableAcl acl = (MutableAcl) aclService.readAclById(identity);

        AccessControlEntry[] entries = acl.getEntries().toArray(new AccessControlEntry[acl.getEntries().size()]);

        for (int i = 0; i < acl.getEntries().size(); i++) {
            if (entries[i].getSid().equals(sid) && entries[i].getPermission().equals(permission)) {
                acl.deleteAce(i);
            }
        }

        aclService.updateAcl(acl);
        log.info("Permission {} was removed from sid {}", permission, sid);
    }

    @Override
    public <T> boolean isPermissionGranted(Class<T> clazz, Serializable identifier, Sid sid, Permission permission) {
        ObjectIdentity identity = new ObjectIdentityImpl(clazz.getCanonicalName(), identifier);
        MutableAcl acl = (MutableAcl) aclService.readAclById(identity);
        boolean isGranted = false;

        try {
            isGranted = acl.isGranted(Arrays.asList(permission), Arrays.asList(sid), false);
        } catch (NotFoundException e) {
            log.info("Unable to find an ACE for the given object", e);
        } catch (UnloadedSidException e) {
            log.error("Unloaded Sid", e);
        }

        return isGranted;
    }

    private MutableAcl isNewAcl(ObjectIdentity identity) {
        MutableAcl acl;
        try {
            acl = (MutableAcl) aclService.readAclById(identity);
        } catch (NotFoundException e) {
            acl = aclService.createAcl(identity);
        }
        return acl;
    }

    private void isPermissionGranted(Permission permission, Sid sid, MutableAcl acl) {
        try {
            acl.isGranted(Arrays.asList(permission), Arrays.asList(sid), false);
        } catch (NotFoundException e) {
            acl.insertAce(acl.getEntries().size(), permission, sid, true);
        }
    }
}
