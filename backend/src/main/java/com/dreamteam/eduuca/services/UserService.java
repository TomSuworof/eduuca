package com.dreamteam.eduuca.services;

import com.dreamteam.eduuca.entities.article.Article;
import com.dreamteam.eduuca.entities.user.role.Role;
import com.dreamteam.eduuca.entities.user.role.RoleEnum;
import com.dreamteam.eduuca.entities.user.User;
import com.dreamteam.eduuca.payload.request.SignupRequest;
import com.dreamteam.eduuca.payload.request.UserDataRequest;
import com.dreamteam.eduuca.payload.response.PageResponseDTO;
import com.dreamteam.eduuca.payload.response.UserDTO;
import com.dreamteam.eduuca.repositories.RoleRepository;
import com.dreamteam.eduuca.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public @NotNull User loadUserByUsername(@NotNull String username) throws EntityNotFoundException {
        log.debug("loadUserByUsername() called. Username: {}", username);

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            log.trace("loadUserByUsername(). User is present. User: {}", userOpt::get);
            return userOpt.get();
        } else {
            log.warn("loadUserByUsername(). User is not present. Will throw exception");
            throw new EntityNotFoundException("User does not exist");
        }
    }

    public @NotNull User loadUserById(@NotNull UUID id) throws EntityNotFoundException {
        log.debug("loadUserById() called. ID: {}", id);

        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            log.trace("loadUserById(). User is present: {}", userOpt::get);
            return userOpt.get();
        } else {
            log.warn("loadUserById(). User is not present. Will throw exception");
            throw new EntityNotFoundException("User does not exist");
        }
    }

    public @NotNull User loadUserByEmail(@NotNull String email) throws EntityNotFoundException {
        log.debug("loadUserByEmail() called. Email: {}", email);

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            log.trace("loadUserByEmail(). User is present: {}", userOpt::get);
            return userOpt.get();
        } else {
            log.warn("loadUserByEmail(). User is not present. Will throw exception");
            throw new EntityNotFoundException("User does not exist");
        }
    }

    public @NotNull User getUserFromAuthentication(@NotNull Authentication authentication) throws EntityNotFoundException {
        log.debug("getUserFromAuthentication() called. Auth: {}", () -> authentication);
        return loadUserByUsername(authentication.getName());
    }

    public UserDTO saveUser(@NotNull SignupRequest signupRequest) {
        log.debug("saveUser() called. Sign uo request: {}", () -> signupRequest);

        if (existsByUsername(signupRequest.getUsername())) {
            log.warn("saveUser(). User with this username '{}' already exists", signupRequest.getUsername());
            throw new IllegalArgumentException("User with this username already exists");
        }

        if (existsByEmail(signupRequest.getEmail())) {
            log.warn("saveUser(). User with this email '{}' already exists", signupRequest.getEmail());
            throw new IllegalArgumentException("User with this email already exists");
        }

        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));

        Optional<Role> roleOpt = roleRepository.findById(RoleEnum.USER.getAsObject().getId());
        if (roleOpt.isEmpty()) {
            log.warn("saveUser(). Role not found");
            throw new IllegalStateException("Role does not exist");
        }
        Role role = roleOpt.get();

        user.setRoles(Set.of(role));

        log.trace("saveUser(). User to save: {}", () -> user);
        saveUser(user);
        log.trace("saveUser(). User successfully saved. User: {}", () -> user);


//        Executors.newSingleThreadExecutor().submit(() -> {
//            try {
//                mailService.sendRegistrationConfirm(user.getEmail(), user.getUsername());
//            } catch (EmailException e) {
//                throw new RuntimeException(e);
//            }
//        });

        return new UserDTO(user);
    }

    private boolean existsByUsername(@NotNull String username) {
        log.debug("existsByUsername() called. Username: {}", username);
        return userRepository.existsByUsername(username);
    }

    private boolean existsByEmail(@NotNull String email) {
        log.debug("existsByEmail() called. Email: {}", email);
        return userRepository.existsByEmail(email);
    }

    public UserDTO updateUser(@NotNull UUID userId, @NotNull UserDataRequest userData) {
        log.debug("updateUser() called. User ID: {}, user data: {}", () -> userId, () -> userData);

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            log.warn("updateUser(). User with ID={} does not exist", userId);
            throw new EntityNotFoundException("User does not exist");
        }

        User userFromDB = userOpt.get();
        log.trace("updateUser(). User from DB found: {}", () -> userFromDB);

        if (userData.getAvatar().isPresent()) {
            log.trace("updateUser(). New user data contains avatar. Setting to old user");
            userFromDB.setAvatar(userData.getAvatar().get());
        }

        if (userData.getEmail().isPresent()) {
            log.trace("updateUser(). New user data contains email. Setting to old user");
            userFromDB.setEmail(userData.getEmail().get());
        }

        if (userData.getBio().isPresent()) {
            log.trace("updateUser(). New user data contains bio. Setting to old user");
            userFromDB.setBio(userData.getBio().get());
        }

        if (userData.getPassword().isPresent()) {
            log.trace("updateUser(). New user data contains password. Setting to old user");
            userFromDB.setPassword(passwordEncoder.encode(userData.getPassword().get()));
        }

        log.trace("updateUser(). User to save: {}", () -> userFromDB);
        saveUser(userFromDB);
        log.trace("updateUser(). User successfully saved. New user: {}", () -> userFromDB);

        return new UserDTO(userFromDB);
    }

    public UserDTO changeRole(@NotNull UUID userId, @NotNull Role role) {
        log.debug("changeRole() called. User ID: {}, role: {}", () -> userId, () -> role);

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            log.warn("changeRole(). User with ID={} does not exist", userId);
            throw new EntityNotFoundException("User does not exist");
        }

        User userFromDB = userOpt.get();
        log.trace("changeRole(). User from DB found: {}", () -> userFromDB);

        userFromDB.setRoles(new HashSet<>(Collections.singletonList(role)));

//        Executors.newSingleThreadExecutor().submit(() -> {
//            try {
//                mailService.sendRoleChanged(userFromDB.getEmail(), role.getName());
//            } catch (EmailException e) {
//                throw new RuntimeException(e);
//            }
//        });

        log.trace("changeRole(). New user to save: {}", () -> userFromDB);
        saveUser(userFromDB);
        log.trace("changeRole(). New user successfully saved. New user: {}", () -> userFromDB);

        return new UserDTO(userFromDB);
    }

    private void saveUser(@NotNull User user) {
        log.debug("saveUser() called. User: {}", () -> user);
        userRepository.save(user);
        log.trace("saveUser(). User saved");
    }

    public boolean isCurrentPasswordSameAs(@NotNull UUID userId, @NotNull String passwordAnother) {
        log.debug("isCurrentPasswordSameAs() called. User ID: {}", userId);
        User requiredUser = loadUserById(userId);
        log.trace("isCurrentPasswordSameAs(). Found user: {}", () -> requiredUser);

        String requiredUserPassword = requiredUser.getPassword();
        return passwordEncoder.matches(passwordAnother, requiredUserPassword);
    }


    public PageResponseDTO<UserDTO> getUsersPaginated(Integer limit, Integer offset) {
        log.debug("getUsersPaginated() called. Limit: {}, offset: {}", limit, offset);
        Page<User> users = getAllUsersPaginated(limit, offset);
        log.trace("getUsersPaginated(). Users: {}", () -> users);

        return new PageResponseDTO<>(
                offset > 0 && users.getTotalElements() > 0,
                (offset + limit) < users.getTotalElements(),
                users.getTotalElements(),
                users.stream().map(UserDTO::new).toList());
    }

    private Page<User> getAllUsersPaginated(Integer limit, Integer offset) {
        log.debug("getAllUsersPaginated() called. Limit: {}, offset: {}", limit, offset);
        return userRepository.findAll(PageRequest.of(offset / limit, limit));
    }

    public boolean canUserEditArticle(@NotNull User user, @NotNull Article article) {
        log.debug("canUserEditArticle() called. User: {}, article: {}", () -> user, () -> article);
        boolean isAdmin = user.is(RoleEnum.ADMIN);
        boolean isModerator = user.is(RoleEnum.MODERATOR);
        boolean isAuthor = user.equals(article.getAuthor());

        log.trace("canUserEditArticle(). isAdmin: {}, isModerator: {}, isAuthor: {}", isAdmin, isModerator, isAuthor);
        return isAdmin || isModerator || isAuthor;
    }
}
