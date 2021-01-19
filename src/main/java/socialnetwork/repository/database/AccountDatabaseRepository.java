package socialnetwork.repository.database;

import socialnetwork.domain.Account;
import socialnetwork.repository.AESUtil;
import socialnetwork.repository.Repository;
import socialnetwork.repository.RepositoryException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class AccountDatabaseRepository implements Repository<String, Account>
{
    private Connection connection;

    public AccountDatabaseRepository(String url, String username, String password)
    {
        try
        {
            this.connection = DriverManager.getConnection(url, username, password);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public Account findOne(String s)
    {
        Account account = null;

        try
        {
            PreparedStatement statement = this.connection.prepareStatement("SELECT * from Cont WHERE username = (?)");

            statement.setString(1, s);

            ResultSet resultSet = statement.executeQuery();

            if(!resultSet.next())
                return null;

            String username = resultSet.getString("username");
            String password = resultSet.getString("password");
            Long userID = resultSet.getLong("idUser");

            String decryptedPassword = AESUtil.decrypt(AESUtil.Algorithm, password, AESUtil.key, AESUtil.generateIv());

            account = new Account(username, decryptedPassword, userID);
            account.setId(username);
        }catch (SQLException | NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e)
        {
            e.printStackTrace();
        }
        return account;
    }

    @Override
    public Iterable<Account> findAll()
    {
        Set<Account> accounts = new HashSet<>();
        try
        {
            PreparedStatement statement = this.connection.prepareStatement("SELECT * from Cont");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next())
            {
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");
                Long userID = resultSet.getLong("idUser");

                Account account = new Account(username, password, userID);
                account.setId(username);
                accounts.add(account);
            }

            return accounts;
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Account save(Account entity)
    {
        if(this.findOne(entity.getId()) != null)
            throw new RepositoryException("Username already exists!");

        try
        {
            IvParameterSpec ivParameterSpec = AESUtil.generateIv();
            String cipherText = AESUtil.encrypt(AESUtil.Algorithm, entity.getPassword(), AESUtil.key, ivParameterSpec);

            PreparedStatement statement = this.connection.prepareStatement("INSERT INTO Cont(username, password, idUser) VALUES (?, ?, ?)");

            statement.setString(1, entity.getId());
            statement.setString(2,cipherText);
            statement.setLong(3, entity.getUserID());

            statement.executeUpdate();
        }catch (SQLException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Account delete(String s)
    {
        Account account = this.findOne(s);
        if(account == null)
            return null;

        try
        {
            PreparedStatement statement = this.connection.prepareStatement("DELETE FROM Cont WHERE username = (?)");

            statement.setString(1, s);

            statement.executeUpdate();
        }catch (SQLException e)
        {
            e.printStackTrace();
        }

        return account;
    }

    @Override
    public Account update(Account entity)
    {
        Account account = this.findOne(entity.getId());
        if(account == null)
            return entity;

        try
        {
            PreparedStatement statement = this.connection.prepareStatement("UPDATE Cont SET password = (?) WHERE username = (?)");

            statement.setString(1, entity.getPassword());
            statement.setString(2, entity.getUsername());

            statement.executeUpdate();
        }catch (SQLException e)
        {
            e.printStackTrace();
        }

        return null;
    }
}
