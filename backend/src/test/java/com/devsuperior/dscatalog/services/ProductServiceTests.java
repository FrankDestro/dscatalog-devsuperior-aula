package com.devsuperior.dscatalog.services;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import com.devsuperior.dscatalog.repositories.ProductRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.tests.Factory;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {
		
	@InjectMocks
	private ProductService service;
	
	@Mock // Injeção de dependencia Simulada. 
	private ProductRepository repository;
	
	@Mock // Injeção de dependencia Simulada. 
	private CategoryRepository categoryRepository;
	

	// criando variaveis fixas para ser utilizada nos testes. 
	private long existingId;
	private long NoExistingId;
	private long dependentId;
	private PageImpl<Product> page;
	private Category category;
	private Product product;
	
	// preparando os dados : Arrange 
	@BeforeEach
	void setUp() throws Exception {
		
		existingId = 1L;
		NoExistingId = 1000L;
		dependentId = 4L;
		product = Factory.createProduct();
		category = Factory.createCategory();
		page = new PageImpl<>(List.of(product));
		
		// Simulação dos comportamentos do Repository 
		
		// Insert
		Mockito.when(repository.save(ArgumentMatchers.any())).thenReturn(product);
		
		// Update // ProductRepository
		Mockito.when(repository.getOne(existingId)).thenReturn(product);
		Mockito.when(repository.getOne(NoExistingId)).thenThrow(EntityNotFoundException.class);
				
				 // categoryRepository
		Mockito.when(categoryRepository.getOne(existingId)).thenReturn(category);
		Mockito.when(categoryRepository.getOne(NoExistingId)).thenThrow(EntityNotFoundException.class);
		
		
		// FindById
		Mockito.when(repository.findById(existingId)).thenReturn(Optional.of(product));
		Mockito.when(repository.findById(NoExistingId)).thenReturn(Optional.empty());
		
		// FindALL
		Mockito.when(repository.findAll((Pageable)ArgumentMatchers.any())).thenReturn(page);	
			
		// Delete
		Mockito.doNothing().when(repository).deleteById(existingId);
		Mockito.doThrow(EmptyResultDataAccessException.class).when(repository).deleteById(NoExistingId);
		Mockito.doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentId);
		
	}
	
	@Test
	public void InsertShouldInsertProductWhenIdExists(){
		
		Product product = Factory.createProduct();
		
		Assertions.assertNotNull(product);
			
	}
	
	@Test
	public void updateShouldReturnProductDTOWhenIdExists(){
		
		ProductDTO productdto = Factory.createProductDTO();
		
		ProductDTO result = service.update(existingId, productdto);
		
		Assertions.assertNotNull(result);
	}
	
	@Test
	public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExists(){
		
		 ProductDTO productdto = Factory.createProductDTO();
		
		 Assertions.assertThrows(ResourceNotFoundException.class,() -> {
			 service.update(NoExistingId, productdto);	
		 });
		
	}
	
	@Test
	public void findByIdShouldReturnProductDTOWhenIdExists() {
		
	 ProductDTO result = service.findById(existingId);
	 
	 Assertions.assertNotNull(result);
	 
	 Mockito.verify(repository).findById(existingId);
		
	}
	
	@Test
	public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExists() {
		
	 Assertions.assertThrows(ResourceNotFoundException.class,() -> {
		 service.findById(NoExistingId);
	 });
	
	 Mockito.verify(repository, Mockito.times(1)).findById(NoExistingId);
		
	}
	
	@Test
	public void findAllPagedShouldReturnPage() {
		
		Pageable pageable = PageRequest.of(0, 10);
		
		Page<ProductDTO> result = service.findAllPaged(pageable);
		
		Assertions.assertNotNull(result);
		Mockito.verify(repository).findAll(pageable);
		
	}

	@Test
	public void deleteShouldThrowDatabaseExceptionWhenWhenDependentId() {
		
		Assertions.assertThrows(DatabaseException.class,() -> {
			service.delete(dependentId);
		});
		
		// verificar se o metodo deleteById do repository foi chamado na acao acima. 
		Mockito.verify(repository, Mockito.times(1)).deleteById(dependentId);
	}	
	
	@Test
	public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExists() {
		
		Assertions.assertThrows(ResourceNotFoundException.class,() -> {
			service.delete(NoExistingId);
		});
		
		// verificar se o metodo deleteById do repository foi chamado na acao acima. 
		Mockito.verify(repository, Mockito.times(1)).deleteById(NoExistingId);
	}			
	
	@Test
	public void deleteShouldDoNothingWhenIdExists() {
		
		Assertions.assertDoesNotThrow(() -> {
			service.delete(existingId);
		});
		
		// verificar se o metodo deleteById do repository foi chamado na acao acima. 
		Mockito.verify(repository, Mockito.times(1)).deleteById(existingId);
	}							 //   |  verificar quantas vezes o metodo deleteById foi chamado.
	
}
