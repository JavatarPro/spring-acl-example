package pro.javatar.security.acl.service;

import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.*;
import org.springframework.util.Assert;

import javax.sql.DataSource;

public class MutableAclServiceImpl extends JdbcMutableAclService {

    private static final String SID_IDENTITY_QUERY = "select currval(pg_get_serial_sequence('acl_sid', 'id'))";
    private static final String GRANT_ACL_QUERY = "insert into acl_entry "
                                                          + "(acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure)"
                                                          + "values (?, ?, ?, ?, ?, ?, ?)";
    private AclCache aclCache;

    public MutableAclServiceImpl(DataSource dataSource, LookupStrategy lookupStrategy, AclCache aclCache) {
        super(dataSource, lookupStrategy, aclCache);
        this.aclCache = aclCache;
        setSidIdentityQuery(SID_IDENTITY_QUERY);
    }

    public Long createOrRetrieveSidPrimaryKey(Sid sid) {
        return super.createOrRetrieveSidPrimaryKey(sid, true);
    }

    public boolean grantPermission(ObjectIdentity objectIdentity, Long sidId, Permission permission, int order) {
        // todo: find solution using code
        boolean isGranted = jdbcOperations.update(GRANT_ACL_QUERY, objectIdentity.getIdentifier(), order, sidId, permission.getMask(), Boolean.TRUE, Boolean.FALSE, Boolean.FALSE) == 1;

        if (isGranted) {
            // Clear the cache, including children
            aclCache.evictFromCache(objectIdentity);

            // Retrieve the ACL via superclass (ensures cache registration, proper retrieval
            // etc)
            Acl acl = readAclById(objectIdentity);
            Assert.isInstanceOf(MutableAcl.class, acl, "MutableAcl should be been returned");
        }
        return isGranted;
    }
}
