package com.accela.pianoforte.model;

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Contact {
    public String company;
    public String street1;
    public String street2;
    public String city;
    public String state;
    public String postCode;
    public String telephone;
    public String email;
}
