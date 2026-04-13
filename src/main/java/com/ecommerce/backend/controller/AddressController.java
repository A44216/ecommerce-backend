package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.requests.AddressRequest;
import com.ecommerce.backend.dto.responses.AddressResponse;
import com.ecommerce.backend.service.AddressService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@CrossOrigin
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    // lấy tất cả địa chỉ
    @GetMapping
    public List<AddressResponse> getAllAddresses() {
        return addressService.getAllAddresses();
    }

    // lấy địa chỉ theo id
    @GetMapping("/{id}")
    public AddressResponse getAddressById(@PathVariable Integer id) {
        return addressService.getAddressById(id);
    }

    // thêm địa chỉ
    @PostMapping
    public AddressResponse createAddress(@RequestBody AddressRequest request) {
        return addressService.createAddress(request);
    }

    // cập nhật địa chỉ
    @PutMapping("/{id}")
    public AddressResponse updateAddress(@PathVariable Integer id,
                                         @RequestBody AddressRequest request) {
        return addressService.updateAddress(id, request);
    }

    // xóa địa chỉ
    @DeleteMapping("/{id}")
    public void deleteAddress(@PathVariable Integer id) {
        addressService.deleteAddress(id);
    }

    // lấy địa chỉ theo user
    @GetMapping("/user/{userId}")
    public List<AddressResponse> getAddressByUser(@PathVariable Integer userId) {
        return addressService.getAddressByUser(userId);
    }
}