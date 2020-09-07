package pro.javatar.security.acl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pro.javatar.security.acl.config.BaseTest;
import pro.javatar.security.acl.config.TestDBConfiguration;
import pro.javatar.security.acl.entity.MenuItem;
import pro.javatar.security.acl.service.MenuItemService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("component-test")
@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(classes = {TestDBConfiguration.class},
        initializers = {SpringAclExampleAppIT.Initializer.class})
@SpringBootTest(classes = SpringAclExampleApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringAclExampleAppIT extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(SpringAclExampleAppIT.class);

    private static final String USER1 = UUID.randomUUID().toString();
    private static final String USER2 = UUID.randomUUID().toString();

    @Autowired
    private MenuItemService menuItemService;

    @Test
    void componentTest() {
        loginAsUser(USER1, "READ");
        Long itemId1 = menuItemService.create(new MenuItem("name1", USER1), USER1);
        List<MenuItem> user1Items = menuItemService.getMyMenuItems();
        assertThat(user1Items, hasSize(1));
        assertThat(user1Items.get(0).getId(), is(itemId1));
        assertThat(user1Items.get(0).getName(), is("name1"));
        assertTrue(menuItemService.hasPermission(itemId1, USER1, BasePermission.READ));

        loginAsUser(USER2, "READ");
        //Try to retrieve user1's items
        List<MenuItem> user2Items = menuItemService.getMyMenuItems();
        assertThat(user2Items, hasSize(0));

        //Create item `name2` and retrieve for user2
        Long itemId2 = menuItemService.create(new MenuItem("name2", USER2), USER2);
        user2Items = menuItemService.getMyMenuItems();
        assertThat(user2Items, hasSize(1));
        assertThat(user2Items.get(0).getId(), is(itemId2));
        assertThat(user2Items.get(0).getName(), is("name2"));

        //try to delete user1's item and get an error
        Assertions.assertThrows(AccessDeniedException.class, () -> menuItemService.delete(itemId1, USER2));

        //share user1's item with user2
        loginAsUser(USER1, "READ");
        menuItemService.grantPermission(itemId1, USER2, BasePermission.READ);

        loginAsUser(USER2, "READ");
        user2Items = menuItemService.getMyMenuItems();
        assertThat(user2Items, hasSize(2));

        //remove permission READ
        loginAsUser(USER1, "READ");
        menuItemService.removePermission(itemId1, USER2, BasePermission.READ);
        loginAsUser(USER2, "READ");
        user2Items = menuItemService.getMyMenuItems();
        assertThat(user2Items, hasSize(1));

        loginAsUser(USER1, "READ");
        menuItemService.delete(itemId1, USER1);

        user1Items = menuItemService.getMyMenuItems();
        assertThat(user1Items, hasSize(0));
    }

    private void loginAsUser(String username, String... authorities) {
        logger.info("--> Login as user: {}", username);
        SecurityContextHolder.getContext().setAuthentication(null);
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (String authority : authorities) {
            grantedAuthorities.add(new SimpleGrantedAuthority(authority));
        }
        Authentication authentication = new UsernamePasswordAuthenticationToken(username, "", grantedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}