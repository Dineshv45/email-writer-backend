package com.email.writer.emaildto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmailRequestDto {

    private String emailContent;
    private String tone;
}
