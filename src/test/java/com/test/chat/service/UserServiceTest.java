package com.test.chat.service;

import com.test.chat.domain.Role;
import com.test.chat.domain.User;
import com.test.chat.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loadUserByUsername_shouldReturnUser_whenUsernameExists() {
        // given
        User expectedUser = new User();
        expectedUser.setId(1L);
        expectedUser.setUsername("testuser");
        expectedUser.setPassword("password");
        expectedUser.setActive(true);
        expectedUser.setRoles(Set.of(Role.USER));

        when(userRepo.findByUsername(anyString())).thenReturn(expectedUser);

        // when
        UserDetails userDetails = userService.loadUserByUsername("testuser");

        // then
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        verify(userRepo, times(1)).findByUsername("testuser");
    }

    @Test
    void loadUserByUsername_shouldThrowException_whenUserNotFound() {
        // given
        when(userRepo.findByUsername(anyString())).thenReturn(null);

        // when & then
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("nonexistent")
        );

        assertEquals("User not found with username: nonexistent", exception.getMessage());
        verify(userRepo, times(1)).findByUsername("nonexistent");
    }

    @Test
    void loadUserByUsername_shouldThrowException_whenNullIsPassed() {
        // given
        when(userRepo.findByUsername(anyString())).thenReturn(null);

        // when & then
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userService.loadUserByUsername(null)
        );

        assertEquals("User not found with username: null", exception.getMessage());
        verify(userRepo, times(1)).findByUsername(null);
    }

    @Test
    void loadUserByUsername_shouldReturnInactiveUser() {
        // given
        User inactiveUser = new User();
        inactiveUser.setId(1L);
        inactiveUser.setUsername("inactiveuser");
        inactiveUser.setPassword("password");
        inactiveUser.setActive(false);
        inactiveUser.setRoles(Set.of(Role.USER));

        when(userRepo.findByUsername(anyString())).thenReturn(inactiveUser);

        // when
        UserDetails userDetails = userService.loadUserByUsername("inactiveuser");

        // then
        assertNotNull(userDetails);
        assertFalse(userDetails.isEnabled());
        verify(userRepo, times(1)).findByUsername("inactiveuser");
    }

    @Test
    void loadUserByUsername_shouldReturnUserWithAdminRole() {
        // given
        User adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setPassword("password");
        adminUser.setActive(true);
        adminUser.setRoles(Set.of(Role.ADMIN));

        when(userRepo.findByUsername(anyString())).thenReturn(adminUser);

        // when
        UserDetails userDetails = userService.loadUserByUsername("admin");

        // then
        assertNotNull(userDetails);
        assertTrue(((User) userDetails).isAdmin());
        verify(userRepo, times(1)).findByUsername("admin");
    }

    @Test
    void loadUserByUsername_shouldReturnUserWithMultipleRoles() {
        // given
        User user = new User();
        user.setId(1L);
        user.setUsername("multirole");
        user.setPassword("password");
        user.setActive(true);
        user.setRoles(Set.of(Role.USER, Role.ADMIN));

        when(userRepo.findByUsername(anyString())).thenReturn(user);

        // when
        UserDetails userDetails = userService.loadUserByUsername("multirole");

        // then
        assertNotNull(userDetails);
        assertEquals(2, userDetails.getAuthorities().size());
        verify(userRepo, times(1)).findByUsername("multirole");
    }

    @Test
    void loadUserByUsername_shouldVerifyUsernameNotCalledWithEmptyString() {
        // given
        User user = new User();
        user.setId(1L);
        user.setUsername("");
        user.setPassword("password");
        user.setActive(true);
        user.setRoles(Set.of(Role.USER));

        when(userRepo.findByUsername(anyString())).thenReturn(user);

        // when
        UserDetails userDetails = userService.loadUserByUsername("");

        // then
        assertNotNull(userDetails);
        assertEquals("", userDetails.getUsername());
        verify(userRepo, times(1)).findByUsername("");
    }

    @Test
    void loadUserByUsername_shouldThrowExceptionWhenRepoReturnsNull() {
        // given
        when(userRepo.findByUsername(anyString())).thenReturn(null);

        // when & then
        assertThrows(
                UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("anyusername")
        );
    }
}