package com.devsuperior.dscatalog.resources;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.services.ProductService;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.tests.Factory;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ProductResource.class)
public class ProductResourceTests {

	@Autowired
	private MockMvc mockMvc; // -> chamar endpoints

	@MockBean
	private ProductService service;
	
	@Autowired
	private ObjectMapper objectMapper;

	// criando variaveis fixas para ser utilizada nos testes.
	private Long existingId;
	private Long NoexistingId;
	private Long dependentId;
	private ProductDTO productdto;
	private PageImpl<ProductDTO> page;

	@BeforeEach
	void setUp() throws Exception {
		existingId = 1L;
		NoexistingId = 1000L;
		dependentId = 3L;
		
		productdto = Factory.createProductDTO();
		page = new PageImpl<>(List.of(productdto));

		// FindALL - simulando comportamento do findAllPaged do service.
		Mockito.when(service.findAllPaged(ArgumentMatchers.any())).thenReturn(page);

		// FindById
		Mockito.when(service.findById(existingId)).thenReturn(productdto);
		// FindById no NoexistingId
		Mockito.when(service.findById(NoexistingId)).thenThrow(ResourceNotFoundException.class);
		
		// Update
		Mockito.when(service.update(eq(existingId),ArgumentMatchers.any())).thenReturn(productdto);
		// Update no NoexistingId
		Mockito.when(service.update(eq(NoexistingId),ArgumentMatchers.any())).thenThrow(ResourceNotFoundException.class);

		// Delete no existingId 
		doNothing().when(service).delete(existingId);
		// Delete no NoexistingId
		doThrow(ResourceNotFoundException.class).when(service).delete(NoexistingId);
		// Delete no dependentId
		doThrow(DatabaseException.class).when(service).delete(dependentId);
		
		// Insert
		Mockito.when(service.insert(ArgumentMatchers.any())).thenReturn(productdto);
		
	}

	@Test
	public void findAllShouldReturnPage() throws Exception {

		// Forma didatica
		ResultActions result = 
				mockMvc.perform(get("/products")
				.accept(MediaType.APPLICATION_JSON));
				

		result.andExpect(status().isOk());

		// Forma simplificada
		mockMvc.perform(get("/products")).andExpect(status().isOk());
	}

	@Test
	public void findByIdShouldReturnProductDTOWhenIdExists() throws Exception {
		
		ResultActions result = 
				mockMvc.perform(get("/products/{id}",existingId)
				.accept(MediaType.APPLICATION_JSON));
				// negociacao de conteudo. 
		
		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.name").exists());
		result.andExpect(jsonPath("$.description").exists());
					// comando para analisar no corpo a resposta json se possui o id,name,descrition e etc. 
		
	}
	
	@Test
	public void findByIdShouldThrowReturnNotFoundExceptionWhenIdDoesNotExists() throws Exception {
		
		ResultActions result = 
				mockMvc.perform(get("/products/{id}",NoexistingId)
				.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isNotFound());
	}
		
	@Test
	public void updateShouldReturnProductDTOWhenIdExists() throws Exception {
		
		String jsonBody = objectMapper.writeValueAsString(productdto);	
		
		ResultActions result = 
				mockMvc.perform(put("/products/{id}",existingId)
				.content(jsonBody)	
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.name").exists());
		result.andExpect(jsonPath("$.description").exists());
					// comando para analisar no corpo a resposta json se possui o id, name, description e etc. 
	}
	
	@Test
	public void updateShouldReturnNotFoundExceptionWhenIdDoesNotExists() throws Exception {
		
		String jsonBody = objectMapper.writeValueAsString(productdto);	
		
		ResultActions result = 
				mockMvc.perform(put("/products/{id}",NoexistingId)
				.content(jsonBody)	
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isNotFound());	
	}
	
	@Test
	public void deleteShouldDoNothingWhenIdExists()throws Exception {
		
	
		ResultActions result = 
				mockMvc.perform(delete("/products/{id}",existingId)
						.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isNoContent());

	}
	
	@Test
	public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExists()throws Exception {
	
		ResultActions result = 
			mockMvc.perform(delete("/products/{id}",NoexistingId)
					.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isNotFound());
	}
	
	@Test
	public void insertShouldReturnProductandReturnCeated()throws Exception {	
		
		String jsonBody = objectMapper.writeValueAsString(productdto);	
		
		ResultActions result = 
				mockMvc.perform(post("/products/")
				.content(jsonBody)	
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isCreated());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.name").exists());
		result.andExpect(jsonPath("$.description").exists());
		
		
		}
	}	
