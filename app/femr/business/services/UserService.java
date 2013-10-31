package femr.business.services;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Query;
import com.google.inject.Inject;
import femr.business.dtos.ServiceResponse;
import femr.common.models.IRole;
import femr.common.models.IUser;
import femr.data.daos.IRepository;
import femr.data.models.User;
import femr.util.encryptions.IPasswordEncryptor;

import java.util.List;

public class UserService implements IUserService {

    private IRepository<IUser> userRepository;
    private IPasswordEncryptor passwordEncryptor;

    @Inject
    public UserService(IRepository<IUser> userRepository, IPasswordEncryptor passwordEncryptor) {
        this.userRepository = userRepository;
        this.passwordEncryptor = passwordEncryptor;
    }
    @Override
    public ServiceResponse<IUser> createUser(IUser user) {
        ServiceResponse<IUser> response = new ServiceResponse<>();
        encryptAndSetUserPassword(user);

        if (userExistsWithEmail(user.getEmail(), response)) {
            return response;
        }

        IUser newUser = userRepository.create(user);
        response.setResponseObject(newUser);

        return response;
    }

    @Override
    public IUser findByEmail(String email) {
        ExpressionList<User> query = getQuery().fetch("roles").where().eq("email", email);

        return userRepository.findOne(query);
    }

    @Override
    public IUser findById(int id) {
        ExpressionList<User> query = getQuery().fetch("roles").where().eq("id", id);

        return userRepository.findOne(query);
    }

    @Override
    public List<? extends IRole> findRolesForUser(int id) {
        ExpressionList<User> query = getQuery().fetch("roles").where().eq("id", id);
        IUser user = userRepository.findOne(query);
        return user.getRoles();
    }

    private Query<User> getQuery() {
        return Ebean.find(User.class);
    }

    private void encryptAndSetUserPassword(IUser user) {
        String encryptedPassword = passwordEncryptor.encryptPassword(user.getPassword());

        user.setPassword(encryptedPassword);
    }

    private boolean userExistsWithEmail(String email, ServiceResponse<IUser> response) {
        IUser existingUser = findByEmail(email);

        if (existingUser != null) {
            response.addError("", "User with email exists.");
            return true;
        }
        return false;
    }
}