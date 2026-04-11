package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.requests.AddressRequest;
import com.ecommerce.backend.dto.responses.AddressResponse;
import com.ecommerce.backend.entity.Address;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.repository.AddressRepository;
import com.ecommerce.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressService(AddressRepository addressRepository,
                          UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    // ENTITY -> RESPONSE
    private AddressResponse mapToDTO(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .fullName(address.getFullName())
                .phone(address.getPhone())
                .addressLine(address.getAddressLine())
                .city(address.getCity())
                .district(address.getDistrict())
                .ward(address.getWard())
                .isDefault(address.getIsDefault())
                .build();
    }

    // REQUEST -> ENTITY
    private void mapRequestToAddress(Address address, AddressRequest request) {

        address.setFullName(request.getFullName().trim());
        address.setPhone(request.getPhone().trim());
        address.setAddressLine(request.getAddressLine().trim());
        address.setCity(request.getCity().trim());
        address.setDistrict(request.getDistrict().trim());
        address.setWard(request.getWard().trim());

        address.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
    }

    private Address getAddressOrThrow(Integer id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found with id: " + id));
    }

    private User getUserOrThrow(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    // lấy tất cả address
    public List<AddressResponse> getAllAddresses() {
        return addressRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // lấy address theo id
    public AddressResponse getAddressById(Integer id) {
        Address address = getAddressOrThrow(id);
        return mapToDTO(address);
    }

    // tạo address
    public AddressResponse createAddress(AddressRequest request) {

        User user = getUserOrThrow(request.getUserId());

        Address address = new Address();

        mapRequestToAddress(address, request);

        address.setUser(user);

        // nếu là default -> bỏ default cũ
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.clearDefaultByUser(user.getId());
        }

        Address saved = addressRepository.save(address);

        return mapToDTO(saved);
    }

    // update address
    public AddressResponse updateAddress(Integer id, AddressRequest request) {

        Address address = getAddressOrThrow(id);

        // KHÔNG cho đổi user
        mapRequestToAddress(address, request);

        // nếu set default -> bỏ default cũ
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.clearDefaultByUser(address.getUser().getId());
        }

        Address updated = addressRepository.save(address);

        return mapToDTO(updated);
    }

    // delete address
    public void deleteAddress(Integer id) {
        Address address = getAddressOrThrow(id);
        addressRepository.delete(address);
    }

    // lấy address theo user
    public List<AddressResponse> getAddressByUser(Integer userId) {

        getUserOrThrow(userId);

        return addressRepository.findByUserId(userId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // lấy default address
    public AddressResponse getDefaultAddress(Integer userId) {

        Address address = addressRepository
                .findByUserIdAndIsDefaultTrue(userId)
                .orElseThrow(() -> new RuntimeException("Default address not found"));

        return mapToDTO(address);
    }
}