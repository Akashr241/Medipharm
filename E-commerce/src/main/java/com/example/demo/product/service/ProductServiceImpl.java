
package com.example.demo.product.service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.product.dto.ProductRequestDto;
import com.example.demo.product.dto.ProductResponseDto;
import com.example.demo.product.entity.Product;
import com.example.demo.product.mapper.ProductMapper;
import com.example.demo.product.repository.ProductRepository;

import org.springframework.stereotype.Service;
import com.example.demo.ai.prescription.util.MedicineSearchRanker;
import java.util.Comparator;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

        private final ProductRepository productRepository;
        private final MedicineSearchRanker medicineSearchRanker;

        public ProductServiceImpl(ProductRepository productRepository,
                MedicineSearchRanker medicineSearchRanker
        ) {
            this.productRepository = productRepository;
            this.medicineSearchRanker = medicineSearchRanker;
        }


  

    @Override
    public ProductResponseDto addProduct(ProductRequestDto dto) {

        Product product = ProductMapper.mapToProduct(dto);

        Product savedProduct =
                productRepository.save(product);

        return ProductMapper
                .mapToResponseDto(savedProduct);
    }

    @Override
    public List<ProductResponseDto> getAllProducts() {

        return productRepository.findAll()
                .stream()
                .map(ProductMapper::mapToResponseDto)
                .toList();
    }

    @Override
    public ProductResponseDto getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with id: " + id));

        return ProductMapper.mapToResponseDto(product);
    }

    @Override
    public ProductResponseDto updateProduct(
            Long id,
            ProductRequestDto dto) {

        Product existingProduct =
                productRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Product not found with id: " + id));

        existingProduct.setName(dto.getName());
        existingProduct.setDescription(dto.getDescription());
        existingProduct.setPrice(dto.getPrice());
        existingProduct.setStock(dto.getStock());
        existingProduct.setCategory(dto.getCategory());

        Product updatedProduct =
                productRepository.save(existingProduct);

        return ProductMapper
                .mapToResponseDto(updatedProduct);
    }
    @Override
    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with id: " + id));

        productRepository.delete(product);
    }
}