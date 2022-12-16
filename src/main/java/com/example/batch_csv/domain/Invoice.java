package com.example.batch_csv.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Invoice {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String number;
    private Double amount;
    private Double discount;
    private Double finalAmount;
    private String location;


}
