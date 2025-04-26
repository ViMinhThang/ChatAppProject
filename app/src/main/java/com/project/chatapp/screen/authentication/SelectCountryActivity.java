package com.project.chatapp.screen.authentication;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hbb20.CountryCodePicker;
import com.project.chatapp.R;
import com.project.chatapp.model.Country.Country;
import com.project.chatapp.model.Country.CountryAdapter;

import java.util.ArrayList;
import java.util.List;

public class SelectCountryActivity extends AppCompatActivity {

    private CountryAdapter countryAdapter;
    private List<Country> countryList = new ArrayList<>();
    private CountryCodePicker ccp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_country);

        ImageView icClose = findViewById(R.id.ic_close);
        TextView tvSelectCountry = findViewById(R.id.tv_select_country);
        EditText etSearch = findViewById(R.id.et_search);
        RecyclerView rvCountryList = findViewById(R.id.rv_country_list);

        // Đặt quốc gia mặc định là Việt Nam
        ccp.setDefaultCountryUsingNameCode("VN");
        ccp.resetToDefaultCountry();

        // Sự kiện nhấn nút close để quay lại màn hình trước đó
        icClose.setOnClickListener(view -> finish());

        rvCountryList.setLayoutManager(new LinearLayoutManager(this));
        countryAdapter = new CountryAdapter(countryList);
        rvCountryList.setAdapter(countryAdapter);

        populateCountryList();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                countryAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void populateCountryList() {
        countryList.add(new Country(getString(R.string.andorra), "+376", com.hbb20.R.drawable.flag_andorra));
        countryList.add(new Country(getString(R.string.united_arab_emirates), "+971", com.hbb20.R.drawable.flag_uae));
        countryList.add(new Country(getString(R.string.afghanistan), "+93", com.hbb20.R.drawable.flag_afghanistan));
        countryList.add(new Country(getString(R.string.antigua_and_barbuda), "+1", com.hbb20.R.drawable.flag_antigua_and_barbuda));
        countryList.add(new Country(getString(R.string.anguilla), "+1", com.hbb20.R.drawable.flag_anguilla));
        countryList.add(new Country(getString(R.string.albania), "+355", com.hbb20.R.drawable.flag_albania));
        countryList.add(new Country(getString(R.string.armenia), "+374", com.hbb20.R.drawable.flag_armenia));
        countryList.add(new Country(getString(R.string.angola), "+244", com.hbb20.R.drawable.flag_angola));
        countryList.add(new Country(getString(R.string.antarctica), "+672", com.hbb20.R.drawable.flag_antarctica));
        countryList.add(new Country(getString(R.string.argentina), "+54", com.hbb20.R.drawable.flag_argentina));
        countryList.add(new Country(getString(R.string.american_samoa), "+1", com.hbb20.R.drawable.flag_american_samoa));
        countryList.add(new Country(getString(R.string.austria), "+43", com.hbb20.R.drawable.flag_austria));
        countryList.add(new Country(getString(R.string.australia), "+61", com.hbb20.R.drawable.flag_australia));
        countryList.add(new Country(getString(R.string.aruba), "+297", com.hbb20.R.drawable.flag_aruba));
        countryList.add(new Country(getString(R.string.aland_islands), "+358", com.hbb20.R.drawable.flag_aland));
        countryList.add(new Country(getString(R.string.azerbaijan), "+994", com.hbb20.R.drawable.flag_azerbaijan));
        countryList.add(new Country(getString(R.string.bosnia), "+387", com.hbb20.R.drawable.flag_bosnia));
        countryList.add(new Country(getString(R.string.barbados), "+1", com.hbb20.R.drawable.flag_barbados));
        countryList.add(new Country(getString(R.string.bangladesh), "+880", com.hbb20.R.drawable.flag_bangladesh));
        countryList.add(new Country(getString(R.string.belgium), "+32", com.hbb20.R.drawable.flag_belgium));
        countryList.add(new Country(getString(R.string.burkina_faso), "+226", com.hbb20.R.drawable.flag_burkina_faso));
        countryList.add(new Country(getString(R.string.bulgaria), "+359", com.hbb20.R.drawable.flag_bulgaria));
        countryList.add(new Country(getString(R.string.bahrain), "+973", com.hbb20.R.drawable.flag_bahrain));
        countryList.add(new Country(getString(R.string.burundi), "+257", com.hbb20.R.drawable.flag_burundi));
        countryList.add(new Country(getString(R.string.benin), "+229", com.hbb20.R.drawable.flag_benin));
        countryList.add(new Country(getString(R.string.saint_barhelemy), "+590", com.hbb20.R.drawable.flag_saint_barthelemy));
        countryList.add(new Country(getString(R.string.bermuda), "+1", com.hbb20.R.drawable.flag_bermuda));
        countryList.add(new Country(getString(R.string.brunei_darussalam), "+673", com.hbb20.R.drawable.flag_brunei));
        countryList.add(new Country(getString(R.string.bolivia), "+591", com.hbb20.R.drawable.flag_bolivia));
        countryList.add(new Country(getString(R.string.brazil), "+55", com.hbb20.R.drawable.flag_brazil));
        countryList.add(new Country(getString(R.string.bahamas), "+1", com.hbb20.R.drawable.flag_bahamas));
        countryList.add(new Country(getString(R.string.bhutan), "+975", com.hbb20.R.drawable.flag_bhutan));
        countryList.add(new Country(getString(R.string.botswana), "+267", com.hbb20.R.drawable.flag_botswana));
        countryList.add(new Country(getString(R.string.belarus), "+375", com.hbb20.R.drawable.flag_belarus));
        countryList.add(new Country(getString(R.string.belize), "+501", com.hbb20.R.drawable.flag_belize));
        countryList.add(new Country(getString(R.string.canada), "+1", com.hbb20.R.drawable.flag_canada));
        countryList.add(new Country(getString(R.string.cocos), "+61", com.hbb20.R.drawable.flag_cocos));
        countryList.add(new Country(getString(R.string.congo_democratic), "+243", com.hbb20.R.drawable.flag_democratic_republic_of_the_congo));
        countryList.add(new Country(getString(R.string.central_african), "+236", com.hbb20.R.drawable.flag_central_african_republic));
        countryList.add(new Country(getString(R.string.congo), "+242", com.hbb20.R.drawable.flag_republic_of_the_congo));
        countryList.add(new Country(getString(R.string.switzerland), "+41", com.hbb20.R.drawable.flag_switzerland));
        countryList.add(new Country(getString(R.string.cote_dlvoire), "+225", com.hbb20.R.drawable.flag_cote_divoire));
        countryList.add(new Country(getString(R.string.cook_islands), "+682", com.hbb20.R.drawable.flag_cook_islands));
        countryList.add(new Country(getString(R.string.chile), "+56", com.hbb20.R.drawable.flag_chile));
        countryList.add(new Country(getString(R.string.cameroon), "+237", com.hbb20.R.drawable.flag_cameroon));
        countryList.add(new Country(getString(R.string.china), "+86", com.hbb20.R.drawable.flag_china));
        countryList.add(new Country(getString(R.string.colombia), "+57", com.hbb20.R.drawable.flag_colombia));
        countryList.add(new Country(getString(R.string.costa_rica), "+506", com.hbb20.R.drawable.flag_costa_rica));
        countryList.add(new Country(getString(R.string.cuba), "+53", com.hbb20.R.drawable.flag_cuba));
        countryList.add(new Country(getString(R.string.cape_verde), "+238", com.hbb20.R.drawable.flag_cape_verde));
        countryList.add(new Country(getString(R.string.curacao), "+599", com.hbb20.R.drawable.flag_curacao));
        countryList.add(new Country(getString(R.string.christmas_island), "+61", com.hbb20.R.drawable.flag_christmas_island));
        countryList.add(new Country(getString(R.string.cyprus), "+357", com.hbb20.R.drawable.flag_cyprus));
        countryList.add(new Country(getString(R.string.czech_republic), "+420", com.hbb20.R.drawable.flag_czech_republic));
        countryList.add(new Country(getString(R.string.germany), "+49", com.hbb20.R.drawable.flag_germany));
        countryList.add(new Country(getString(R.string.djibouti), "+253", com.hbb20.R.drawable.flag_djibouti));
        countryList.add(new Country(getString(R.string.denmark), "+45", com.hbb20.R.drawable.flag_denmark));
        countryList.add(new Country(getString(R.string.dominica), "+1", com.hbb20.R.drawable.flag_dominica));
        countryList.add(new Country(getString(R.string.dominician_republic), "+1", com.hbb20.R.drawable.flag_dominican_republic));
        countryList.add(new Country(getString(R.string.algeria), "+213", com.hbb20.R.drawable.flag_algeria));
        countryList.add(new Country(getString(R.string.ecuador), "+593", com.hbb20.R.drawable.flag_ecuador));
        countryList.add(new Country(getString(R.string.estonia), "+372", com.hbb20.R.drawable.flag_estonia));
        countryList.add(new Country(getString(R.string.egypt), "+20", com.hbb20.R.drawable.flag_egypt));
        countryList.add(new Country(getString(R.string.eritrea), "+291", com.hbb20.R.drawable.flag_eritrea));
        countryList.add(new Country(getString(R.string.spain), "+34", com.hbb20.R.drawable.flag_spain));
        countryList.add(new Country(getString(R.string.ethiopia), "+251", com.hbb20.R.drawable.flag_ethiopia));
        countryList.add(new Country(getString(R.string.finland), "+358", com.hbb20.R.drawable.flag_finland));
        countryList.add(new Country(getString(R.string.fiji), "+679", com.hbb20.R.drawable.flag_fiji));
        countryList.add(new Country(getString(R.string.falkland_islands), "+500", com.hbb20.R.drawable.flag_falkland_islands));
        countryList.add(new Country(getString(R.string.micro), "+691", com.hbb20.R.drawable.flag_micronesia));
        countryList.add(new Country(getString(R.string.faroe_islands), "+298", com.hbb20.R.drawable.flag_faroe_islands));
        countryList.add(new Country(getString(R.string.france), "+33", com.hbb20.R.drawable.flag_france));
        countryList.add(new Country(getString(R.string.gabon), "+241", com.hbb20.R.drawable.flag_gabon));
        countryList.add(new Country(getString(R.string.united_kingdom), "+44", com.hbb20.R.drawable.flag_united_kingdom));
        countryList.add(new Country(getString(R.string.grenada), "+1", com.hbb20.R.drawable.flag_grenada));
        countryList.add(new Country(getString(R.string.georgia), "+995", com.hbb20.R.drawable.flag_georgia));
        countryList.add(new Country(getString(R.string.french_guyana), "+594", com.hbb20.R.drawable.flag_guyana));
        countryList.add(new Country(getString(R.string.ghana), "+233", com.hbb20.R.drawable.flag_ghana));
        countryList.add(new Country(getString(R.string.gibraltar), "+350", com.hbb20.R.drawable.flag_gibraltar));
        countryList.add(new Country(getString(R.string.greenland), "+299", com.hbb20.R.drawable.flag_greenland));
        countryList.add(new Country(getString(R.string.gambia), "+220", com.hbb20.R.drawable.flag_gambia));
        countryList.add(new Country(getString(R.string.guinea), "+224", com.hbb20.R.drawable.flag_guinea));
        countryList.add(new Country(getString(R.string.guadeloupe), "+450", com.hbb20.R.drawable.flag_guadeloupe));
        countryList.add(new Country(getString(R.string.equatorial_guinea), "+240", com.hbb20.R.drawable.flag_equatorial_guinea));
        countryList.add(new Country(getString(R.string.greece), "+30", com.hbb20.R.drawable.flag_greece));
        countryList.add(new Country(getString(R.string.guatemala), "+502", com.hbb20.R.drawable.flag_guatemala));
        countryList.add(new Country(getString(R.string.guam), "+1", com.hbb20.R.drawable.flag_guam));
        countryList.add(new Country(getString(R.string.guinea_bissau), "+245", com.hbb20.R.drawable.flag_guinea_bissau));
        countryList.add(new Country(getString(R.string.guyana), "+592", com.hbb20.R.drawable.flag_guyana));
        countryList.add(new Country(getString(R.string.hong_kong), "+852", com.hbb20.R.drawable.flag_hong_kong));
        countryList.add(new Country(getString(R.string.honduras), "+504", com.hbb20.R.drawable.flag_honduras));
        countryList.add(new Country(getString(R.string.croatia), "+385", com.hbb20.R.drawable.flag_croatia));
        countryList.add(new Country(getString(R.string.haiti), "+509", com.hbb20.R.drawable.flag_haiti));
        countryList.add(new Country(getString(R.string.hungary), "+36", com.hbb20.R.drawable.flag_hungary));
        countryList.add(new Country(getString(R.string.indonesia), "+62", com.hbb20.R.drawable.flag_indonesia));
        countryList.add(new Country(getString(R.string.ireland), "+353", com.hbb20.R.drawable.flag_ireland));
        countryList.add(new Country(getString(R.string.israil), "+972", com.hbb20.R.drawable.flag_israel));
        countryList.add(new Country(getString(R.string.isle_of_man), "+44", com.hbb20.R.drawable.flag_isleof_man));
        countryList.add(new Country(getString(R.string.iceland), "+354", com.hbb20.R.drawable.flag_iceland));
        countryList.add(new Country(getString(R.string.india), "+91", com.hbb20.R.drawable.flag_india));
        countryList.add(new Country(getString(R.string.british_indian_ocean), "+246", com.hbb20.R.drawable.flag_british_indian_ocean_territory));
        countryList.add(new Country(getString(R.string.iraq), "+964", com.hbb20.R.drawable.flag_iraq));
        countryList.add(new Country(getString(R.string.iran), "+98", com.hbb20.R.drawable.flag_iran));
        countryList.add(new Country(getString(R.string.italia), "+39", com.hbb20.R.drawable.flag_italy));
        countryList.add(new Country(getString(R.string.jersey), "+44", com.hbb20.R.drawable.flag_jersey));
        countryList.add(new Country(getString(R.string.jamaica), "+1", com.hbb20.R.drawable.flag_jamaica));
        countryList.add(new Country(getString(R.string.jordan), "+962", com.hbb20.R.drawable.flag_jordan));
        countryList.add(new Country(getString(R.string.japan), "+81", com.hbb20.R.drawable.flag_japan));
        countryList.add(new Country(getString(R.string.kenya), "+254", com.hbb20.R.drawable.flag_kenya));
        countryList.add(new Country(getString(R.string.kyrgyzstan), "+996", com.hbb20.R.drawable.flag_kyrgyzstan));
        countryList.add(new Country(getString(R.string.cambodia), "+855", com.hbb20.R.drawable.flag_cambodia));
        countryList.add(new Country(getString(R.string.kiribati), "+686", com.hbb20.R.drawable.flag_kiribati));
        countryList.add(new Country(getString(R.string.comoros), "+269", com.hbb20.R.drawable.flag_comoros));
        countryList.add(new Country(getString(R.string.saint_kitts), "+1", com.hbb20.R.drawable.flag_saint_kitts_and_nevis));
        countryList.add(new Country(getString(R.string.north_korea), "+850", com.hbb20.R.drawable.flag_north_korea));
        countryList.add(new Country(getString(R.string.south_korea), "+82", com.hbb20.R.drawable.flag_south_korea));
        countryList.add(new Country(getString(R.string.kuwait), "+965", com.hbb20.R.drawable.flag_kuwait));
        countryList.add(new Country(getString(R.string.cayman_islands), "+1", com.hbb20.R.drawable.flag_cayman_islands));
        countryList.add(new Country(getString(R.string.kazakhstan), "+7", com.hbb20.R.drawable.flag_kazakhstan));
        countryList.add(new Country(getString(R.string.laos), "+856", com.hbb20.R.drawable.flag_laos));
        countryList.add(new Country(getString(R.string.lebanon), "+961", com.hbb20.R.drawable.flag_lebanon));
        countryList.add(new Country(getString(R.string.saint_lucia), "+1", com.hbb20.R.drawable.flag_saint_lucia));
        countryList.add(new Country(getString(R.string.liechtenstein), "+423", com.hbb20.R.drawable.flag_liechtenstein));
        countryList.add(new Country(getString(R.string.siri_lanka), "+94", com.hbb20.R.drawable.flag_sri_lanka));
        countryList.add(new Country(getString(R.string.liberia), "+231", com.hbb20.R.drawable.flag_liberia));
        countryList.add(new Country(getString(R.string.lesotho), "+266", com.hbb20.R.drawable.flag_lesotho));
        countryList.add(new Country(getString(R.string.lithuania), "+370", com.hbb20.R.drawable.flag_lithuania));
        countryList.add(new Country(getString(R.string.luxembourg), "+352", com.hbb20.R.drawable.flag_luxembourg));
        countryList.add(new Country(getString(R.string.latvia), "+371", com.hbb20.R.drawable.flag_latvia));
        countryList.add(new Country(getString(R.string.libya), "+218", com.hbb20.R.drawable.flag_libya));
        countryList.add(new Country(getString(R.string.marocco), "+212", com.hbb20.R.drawable.flag_morocco));
        countryList.add(new Country(getString(R.string.monaco), "+377", com.hbb20.R.drawable.flag_monaco));
        countryList.add(new Country(getString(R.string.moldova), "+373", com.hbb20.R.drawable.flag_moldova));
        countryList.add(new Country(getString(R.string.montenegro), "+382", com.hbb20.R.drawable.flag_of_montenegro));
        countryList.add(new Country(getString(R.string.saint_martin), "+590", com.hbb20.R.drawable.flag_saint_martin));
        countryList.add(new Country(getString(R.string.madagascar), "+261", com.hbb20.R.drawable.flag_madagascar));
        countryList.add(new Country(getString(R.string.marshall_islands), "+692", com.hbb20.R.drawable.flag_marshall_islands));
        countryList.add(new Country(getString(R.string.north_macedonia), "+389", com.hbb20.R.drawable.flag_macedonia));
        countryList.add(new Country(getString(R.string.mali), "+223", com.hbb20.R.drawable.flag_mali));
        countryList.add(new Country(getString(R.string.myanmar), "+95", com.hbb20.R.drawable.flag_myanmar));
        countryList.add(new Country(getString(R.string.mongolia), "+976", com.hbb20.R.drawable.flag_mongolia));
        countryList.add(new Country(getString(R.string.macau), "+853", com.hbb20.R.drawable.flag_macao));
        countryList.add(new Country(getString(R.string.northern_mariana), "+1", com.hbb20.R.drawable.flag_northern_mariana_islands));
        countryList.add(new Country(getString(R.string.martinique), "+596", com.hbb20.R.drawable.flag_martinique));
        countryList.add(new Country(getString(R.string.mauriatana), "+222", com.hbb20.R.drawable.flag_mauritania));
        countryList.add(new Country(getString(R.string.montserrat), "+1", com.hbb20.R.drawable.flag_montserrat));
        countryList.add(new Country(getString(R.string.malta), "+356", com.hbb20.R.drawable.flag_malta));
        countryList.add(new Country(getString(R.string.mauritius), "+230", com.hbb20.R.drawable.flag_mauritius));
        countryList.add(new Country(getString(R.string.maldives), "+960", com.hbb20.R.drawable.flag_maldives));
        countryList.add(new Country(getString(R.string.malawi), "+265", com.hbb20.R.drawable.flag_malawi));
        countryList.add(new Country(getString(R.string.mexico), "+52", com.hbb20.R.drawable.flag_mexico));
        countryList.add(new Country(getString(R.string.malaysia), "+60", com.hbb20.R.drawable.flag_malaysia));
        countryList.add(new Country(getString(R.string.mozambique), "+258", com.hbb20.R.drawable.flag_mozambique));
        countryList.add(new Country(getString(R.string.namibia), "+264", com.hbb20.R.drawable.flag_namibia));
        countryList.add(new Country(getString(R.string.new_caledonia), "+687", com.hbb20.R.drawable.flag_new_caledonia));
        countryList.add(new Country(getString(R.string.niger), "+227", com.hbb20.R.drawable.flag_niger));
        countryList.add(new Country(getString(R.string.norfolk), "+672", com.hbb20.R.drawable.flag_norfolk_island));
        countryList.add(new Country(getString(R.string.nigeria), "+234", com.hbb20.R.drawable.flag_nigeria));
        countryList.add(new Country(getString(R.string.nicaragua), "+505", com.hbb20.R.drawable.flag_nicaragua));
        countryList.add(new Country(getString(R.string.netherlands), "+31", com.hbb20.R.drawable.flag_netherlands));
        countryList.add(new Country(getString(R.string.norway), "+47", com.hbb20.R.drawable.flag_norway));
        countryList.add(new Country(getString(R.string.nepal), "+977", com.hbb20.R.drawable.flag_nepal));
        countryList.add(new Country(getString(R.string.nauru), "+674", com.hbb20.R.drawable.flag_nauru));
        countryList.add(new Country(getString(R.string.niue), "+683", com.hbb20.R.drawable.flag_niue));
        countryList.add(new Country(getString(R.string.new_zealand), "+64", com.hbb20.R.drawable.flag_new_zealand));
        countryList.add(new Country(getString(R.string.oman), "+968", com.hbb20.R.drawable.flag_oman));
        countryList.add(new Country(getString(R.string.panama), "+507", com.hbb20.R.drawable.flag_panama));
        countryList.add(new Country(getString(R.string.peru), "+51", com.hbb20.R.drawable.flag_peru));
        countryList.add(new Country(getString(R.string.french_polynesia), "+689", com.hbb20.R.drawable.flag_french_polynesia));
        countryList.add(new Country(getString(R.string.papua_new_guinea), "+675", com.hbb20.R.drawable.flag_papua_new_guinea));
        countryList.add(new Country(getString(R.string.philippinies), "+63", com.hbb20.R.drawable.flag_philippines));
        countryList.add(new Country(getString(R.string.pakistan), "+92", com.hbb20.R.drawable.flag_pakistan));
        countryList.add(new Country(getString(R.string.poland), "+48", com.hbb20.R.drawable.flag_poland));
        countryList.add(new Country(getString(R.string.saint_pierre), "+508", com.hbb20.R.drawable.flag_saint_pierre));
        countryList.add(new Country(getString(R.string.pitcairn), "+870", com.hbb20.R.drawable.flag_pitcairn_islands));
        countryList.add(new Country(getString(R.string.puerto_rico), "+1", com.hbb20.R.drawable.flag_puerto_rico));
        countryList.add(new Country(getString(R.string.state_of_palestine), "+970", com.hbb20.R.drawable.flag_palestine));
        countryList.add(new Country(getString(R.string.portugal), "+351", com.hbb20.R.drawable.flag_portugal));
        countryList.add(new Country(getString(R.string.palau), "+680", com.hbb20.R.drawable.flag_palau));
        countryList.add(new Country(getString(R.string.paraguay), "+595", com.hbb20.R.drawable.flag_paraguay));
        countryList.add(new Country(getString(R.string.qatar), "+974", com.hbb20.R.drawable.flag_qatar));
        countryList.add(new Country(getString(R.string.romania), "+40", com.hbb20.R.drawable.flag_romania));
        countryList.add(new Country(getString(R.string.serbia), "+381", com.hbb20.R.drawable.flag_serbia));
        countryList.add(new Country(getString(R.string.russia), "+7", com.hbb20.R.drawable.flag_russian_federation));
        countryList.add(new Country(getString(R.string.rwanda), "+250", com.hbb20.R.drawable.flag_rwanda));
        countryList.add(new Country(getString(R.string.saudi_arabia), "+966", com.hbb20.R.drawable.flag_saudi_arabia));
        countryList.add(new Country(getString(R.string.solomon_islands), "+677", com.hbb20.R.drawable.flag_soloman_islands));
        countryList.add(new Country(getString(R.string.seychelles), "+248", com.hbb20.R.drawable.flag_seychelles));
        countryList.add(new Country(getString(R.string.sudan), "+249", com.hbb20.R.drawable.flag_sudan));
        countryList.add(new Country(getString(R.string.sweden), "+46", com.hbb20.R.drawable.flag_sweden));
        countryList.add(new Country(getString(R.string.singapore), "+65", com.hbb20.R.drawable.flag_singapore));
        countryList.add(new Country(getString(R.string.saint_helena), "+290", com.hbb20.R.drawable.flag_saint_helena));
        countryList.add(new Country(getString(R.string.slovenia), "+386", com.hbb20.R.drawable.flag_slovenia));
        countryList.add(new Country(getString(R.string.slovakia), "+421", com.hbb20.R.drawable.flag_slovakia));
        countryList.add(new Country(getString(R.string.sierra_leone), "+232", com.hbb20.R.drawable.flag_sierra_leone));
        countryList.add(new Country(getString(R.string.san_marino), "+378", com.hbb20.R.drawable.flag_san_marino));
        countryList.add(new Country(getString(R.string.senegal), "+221", com.hbb20.R.drawable.flag_senegal));
        countryList.add(new Country(getString(R.string.somali), "+252", com.hbb20.R.drawable.flag_somalia));
        countryList.add(new Country(getString(R.string.suriname), "+597", com.hbb20.R.drawable.flag_suriname));
        countryList.add(new Country(getString(R.string.south_sudan), "+211", com.hbb20.R.drawable.flag_south_sudan));
        countryList.add(new Country(getString(R.string.sao_tome), "+239", com.hbb20.R.drawable.flag_sao_tome_and_principe));
        countryList.add(new Country(getString(R.string.el_salvador), "+503", com.hbb20.R.drawable.flag_el_salvador));
        countryList.add(new Country(getString(R.string.sint_maarten), "+1", com.hbb20.R.drawable.flag_sint_maarten));
        countryList.add(new Country(getString(R.string.syrian), "+963", com.hbb20.R.drawable.flag_syria));
        countryList.add(new Country(getString(R.string.swaziland), "+268", com.hbb20.R.drawable.flag_swaziland));
        countryList.add(new Country(getString(R.string.turks_and_caicos), "+1", com.hbb20.R.drawable.flag_turks_and_caicos_islands));
        countryList.add(new Country(getString(R.string.chad), "+235", com.hbb20.R.drawable.flag_chad));
        countryList.add(new Country(getString(R.string.togo), "+228", com.hbb20.R.drawable.flag_togo));
        countryList.add(new Country(getString(R.string.thailand), "+66", com.hbb20.R.drawable.flag_thailand));
        countryList.add(new Country(getString(R.string.tokelau), "+690", com.hbb20.R.drawable.flag_tokelau));
        countryList.add(new Country(getString(R.string.timor_leste), "+670", com.hbb20.R.drawable.flag_timor_leste));
        countryList.add(new Country(getString(R.string.turkmenistan), "+993", com.hbb20.R.drawable.flag_turkmenistan));
        countryList.add(new Country(getString(R.string.tunisia), "+216", com.hbb20.R.drawable.flag_tunisia));
        countryList.add(new Country(getString(R.string.tonga), "+676", com.hbb20.R.drawable.flag_tokelau));
        countryList.add(new Country(getString(R.string.turkey), "+90", com.hbb20.R.drawable.flag_turkey));
        countryList.add(new Country(getString(R.string.trinidad_and_tobago), "+1", com.hbb20.R.drawable.flag_trinidad_and_tobago));
        countryList.add(new Country(getString(R.string.tuvalu), "+688", com.hbb20.R.drawable.flag_tuvalu));
        countryList.add(new Country(getString(R.string.taiwan), "+886", com.hbb20.R.drawable.flag_taiwan));
        countryList.add(new Country(getString(R.string.tazmania), "+255", com.hbb20.R.drawable.flag_tanzania));
        countryList.add(new Country(getString(R.string.ukraina), "+380", com.hbb20.R.drawable.flag_ukraine));
        countryList.add(new Country(getString(R.string.uganda), "+256", com.hbb20.R.drawable.flag_uganda));
        countryList.add(new Country(getString(R.string.united_states_america), "+1", com.hbb20.R.drawable.flag_united_states_of_america));
        countryList.add(new Country(getString(R.string.uruguay), "+598", com.hbb20.R.drawable.flag_uruguay));
        countryList.add(new Country(getString(R.string.uzbekistan), "+998", com.hbb20.R.drawable.flag_uzbekistan));
        countryList.add(new Country(getString(R.string.holy_see), "+379", com.hbb20.R.drawable.flag_vatican_city));
        countryList.add(new Country(getString(R.string.saint_vincent), "+1", com.hbb20.R.drawable.flag_saint_vicent_and_the_grenadines));
        countryList.add(new Country(getString(R.string.venezuela), "+58", com.hbb20.R.drawable.flag_venezuela));
        countryList.add(new Country(getString(R.string.british_indian_ocean), "+1", com.hbb20.R.drawable.flag_british_indian_ocean_territory));
        countryList.add(new Country(getString(R.string.virgin_island_us), "+1", com.hbb20.R.drawable.flag_us_virgin_islands));
        countryList.add(new Country(getString(R.string.vietnam), "+84", com.hbb20.R.drawable.flag_vietnam));
        countryList.add(new Country(getString(R.string.vanuatu), "+678", com.hbb20.R.drawable.flag_vanuatu));
        countryList.add(new Country(getString(R.string.walli_and_fatuna), "+681", com.hbb20.R.drawable.flag_wallis_and_futuna));
        countryList.add(new Country(getString(R.string.samoa), "4685", com.hbb20.R.drawable.flag_samoa));
        countryList.add(new Country(getString(R.string.kosovo), "+383", com.hbb20.R.drawable.flag_kosovo));
        countryList.add(new Country(getString(R.string.yemen), "+967", com.hbb20.R.drawable.flag_yemen));
        countryList.add(new Country(getString(R.string.south_africa), "+27", com.hbb20.R.drawable.flag_south_africa));
        countryList.add(new Country(getString(R.string.zambia), "+260", com.hbb20.R.drawable.flag_zambia));
        countryList.add(new Country(getString(R.string.zimbabwe), "+263", com.hbb20.R.drawable.flag_zimbabwe));
        countryAdapter.notifyDataSetChanged();
    }
}