\connect template_postgis
DROP SCHEMA IF EXISTS tiger CASCADE;
DROP SCHEMA IF EXISTS tiger_data CASCADE;
\connect sos
DROP SCHEMA IF EXISTS tiger CASCADE;
DROP SCHEMA IF EXISTS tiger_data CASCADE;
CREATE TABLE public.category (
    category_id bigint NOT NULL,
    identifier character varying(255) NOT NULL,
    name character varying(255),
    description character varying(255)
);
ALTER TABLE public.category OWNER TO postgres;
CREATE TABLE public.category_i18 (
    category_i18n_id bigint NOT NULL,
    fk_category_id bigint NOT NULL,
    locale character varying(255),
    name character varying(255),
    description character varying(255)
);
ALTER TABLE public.category_i18 OWNER TO postgres;
CREATE TABLE public.codespace (
    codespace_id bigint NOT NULL,
    name character varying(255) NOT NULL
);
ALTER TABLE public.codespace OWNER TO postgres;
CREATE TABLE public.composite_observation (
    fk_parent_observation_id bigint NOT NULL,
    fk_child_observation_id bigint NOT NULL
);
ALTER TABLE public.composite_observation OWNER TO postgres;
CREATE TABLE public.composite_phenomenon (
    fk_parent_phenomenon_id bigint NOT NULL,
    fk_child_phenomenon_id bigint NOT NULL
);
ALTER TABLE public.composite_phenomenon OWNER TO postgres;
CREATE TABLE public.dataset (
    dataset_id bigint NOT NULL,
    value_type character varying(255) NOT NULL,
    fk_procedure_id bigint NOT NULL,
    fk_phenomenon_id bigint NOT NULL,
    fk_offering_id bigint NOT NULL,
    fk_category_id bigint,
    fk_feature_id bigint,
    fk_format_id bigint,
    first_time timestamp without time zone,
    last_time timestamp without time zone,
    first_value numeric(29,2),
    last_value numeric(29,2),
    is_deleted smallint DEFAULT 0 NOT NULL,
    is_disabled smallint DEFAULT 0 NOT NULL,
    is_published smallint DEFAULT 1 NOT NULL,
    is_hidden smallint DEFAULT 0 NOT NULL,
    identifier character varying(255),
    fk_identifier_codespace_id bigint,
    name character varying(255),
    fk_name_codespace_id bigint,
    description character varying(255),
    fk_first_observation_id bigint,
    fk_last_observation_id bigint,
    decimals integer,
    fk_unit_id bigint,
    CONSTRAINT dataset_is_deleted_check CHECK ((is_deleted = ANY (ARRAY[1, 0]))),
    CONSTRAINT dataset_is_disabled_check CHECK ((is_disabled = ANY (ARRAY[1, 0]))),
    CONSTRAINT dataset_is_hidden_check CHECK ((is_hidden = ANY (ARRAY[1, 0]))),
    CONSTRAINT dataset_is_published_check CHECK ((is_published = ANY (ARRAY[1, 0]))),
    CONSTRAINT dataset_value_type_check CHECK (((value_type)::text = ANY ((ARRAY['quantity'::character varying, 'count'::character varying, 'text'::character varying, 'category'::character varying, 'boolean'::character varying, 'quantity-profile'::character varying, 'text-profile'::character varying, 'category-profile'::character varying, 'complex'::character varying, 'dataarray'::character varying, 'geometry'::character varying, 'blob'::character varying, 'referenced'::character varying, 'not_initialized'::character varying])::text[])))
);
ALTER TABLE public.dataset OWNER TO postgres;
CREATE TABLE public.dataset_parameter (
    fk_dataset_id bigint NOT NULL,
    fk_parameter_id bigint NOT NULL
);
ALTER TABLE public.dataset_parameter OWNER TO postgres;
CREATE TABLE public.dataset_reference (
    fk_dataset_id_from bigint NOT NULL,
    sort_order integer NOT NULL,
    fk_dataset_id_to bigint NOT NULL
);
ALTER TABLE public.dataset_reference OWNER TO postgres;
CREATE TABLE public.feature (
    feature_id bigint NOT NULL,
    discriminator character varying(255),
    fk_format_id bigint NOT NULL,
    identifier character varying(255),
    fk_identifier_codespace_id bigint,
    name character varying(255),
    fk_name_codespace_id bigint,
    description character varying(255),
    xml text,
    url character varying(255),
    geom public.geometry
);
ALTER TABLE public.feature OWNER TO postgres;
CREATE TABLE public.feature_hierarchy (
    fk_parent_feature_id bigint NOT NULL,
    fk_child_feature_id bigint NOT NULL
);
ALTER TABLE public.feature_hierarchy OWNER TO postgres;
CREATE TABLE public.feature_i18n (
    feature_i18n_id bigint NOT NULL,
    fk_feature_id bigint NOT NULL,
    locale character varying(255),
    name character varying(255),
    description character varying(255)
);
ALTER TABLE public.feature_i18n OWNER TO postgres;
CREATE TABLE public.feature_parameter (
    fk_feature_id bigint NOT NULL,
    fk_parameter_id bigint NOT NULL
);
ALTER TABLE public.feature_parameter OWNER TO postgres;
CREATE TABLE public.format (
    format_id bigint NOT NULL,
    definition character varying(255) NOT NULL
);
ALTER TABLE public.format OWNER TO postgres;
CREATE TABLE public.observation (
    observation_id bigint NOT NULL,
    value_type character varying(255) NOT NULL,
    fk_dataset_id bigint NOT NULL,
    sampling_time_start timestamp without time zone NOT NULL,
    sampling_time_end timestamp without time zone NOT NULL,
    result_time timestamp without time zone NOT NULL,
    identifier character varying(255),
    fk_identifier_codespace_id bigint,
    name character varying(255),
    fk_name_codespace_id bigint,
    description character varying(255),
    is_deleted smallint DEFAULT 0 NOT NULL,
    valid_time_start timestamp without time zone,
    valid_time_end timestamp without time zone,
    is_child smallint DEFAULT 0 NOT NULL,
    is_parent smallint DEFAULT 0 NOT NULL,
    sampling_geometry public.geometry,
    value_identifier character varying(255),
    value_name character varying(255),
    value_description character varying(255),
    vertical_from numeric(19,2) DEFAULT '-99999.00'::numeric NOT NULL,
    vertical_to numeric(19,2) DEFAULT '-99999.00'::numeric NOT NULL,
    value_quantity numeric(29,2),
    value_text character varying(255),
    value_referenced character varying(255),
    value_count integer,
    value_boolean smallint,
    value_category character varying(255),
    value_geometry public.geometry,
    CONSTRAINT observation_is_child_check CHECK ((is_child = ANY (ARRAY[1, 0]))),
    CONSTRAINT observation_is_deleted_check CHECK ((is_deleted = ANY (ARRAY[1, 0]))),
    CONSTRAINT observation_is_parent_check CHECK ((is_parent = ANY (ARRAY[1, 0]))),
    CONSTRAINT observation_value_type_check CHECK (((value_type)::text = ANY ((ARRAY['quantity'::character varying, 'count'::character varying, 'text'::character varying, 'category'::character varying, 'boolean'::character varying, 'profile'::character varying, 'complex'::character varying, 'dataarray'::character varying, 'geometry'::character varying, 'blob'::character varying, 'referenced'::character varying])::text[])))
);
ALTER TABLE public.observation OWNER TO postgres;
CREATE TABLE public.observation_parameter (
    fk_observation_id bigint NOT NULL,
    fk_parameter_id bigint NOT NULL
);
ALTER TABLE public.observation_parameter OWNER TO postgres;
CREATE TABLE public.offering (
    offering_id bigint NOT NULL,
    identifier character varying(255) NOT NULL,
    fk_identifier_codespace_id bigint,
    name character varying(255),
    fk_name_codespace_id bigint,
    description character varying(255),
    sampling_time_start timestamp without time zone,
    sampling_time_end timestamp without time zone,
    result_time_start timestamp without time zone,
    result_time_end timestamp without time zone,
    valid_time_start timestamp without time zone,
    valid_time_end timestamp without time zone,
    geom public.geometry
);
ALTER TABLE public.offering OWNER TO postgres;
CREATE TABLE public.offering_feature_type (
    fk_offering_id bigint NOT NULL,
    fk_format_id bigint NOT NULL
);
ALTER TABLE public.offering_feature_type OWNER TO postgres;
CREATE TABLE public.offering_hierarchy (
    fk_parent_offering_id bigint NOT NULL,
    fk_child_offering_id bigint NOT NULL
);
ALTER TABLE public.offering_hierarchy OWNER TO postgres;
CREATE TABLE public.offering_i18n (
    offering_i18n_id bigint NOT NULL,
    fk_offering_id bigint NOT NULL,
    locale character varying(255),
    name character varying(255),
    description character varying(255)
);
ALTER TABLE public.offering_i18n OWNER TO postgres;
CREATE TABLE public.offering_observation_type (
    fk_offering_id bigint NOT NULL,
    fk_format_id bigint NOT NULL
);
ALTER TABLE public.offering_observation_type OWNER TO postgres;
CREATE TABLE public.offering_related_feature (
    fk_offering_id bigint NOT NULL,
    fk_related_feature_id bigint NOT NULL
);
ALTER TABLE public.offering_related_feature OWNER TO postgres;
CREATE TABLE public.parameter (
    parameter_id bigint NOT NULL,
    type character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    last_update timestamp without time zone,
    domain character varying(255),
    value_boolean smallint,
    value_category character varying(255),
    fk_unit_id bigint,
    value_count integer,
    value_quantity numeric(19,2),
    value_text character varying(255),
    value_xml text,
    value_json text,
    CONSTRAINT parameter_type_check CHECK (((type)::text = ANY ((ARRAY['boolean'::character varying, 'category'::character varying, 'count'::character varying, 'quantity'::character varying, 'text'::character varying, 'xml'::character varying, 'json'::character varying])::text[])))
);
ALTER TABLE public.parameter OWNER TO postgres;
CREATE TABLE public.phenomenon (
    phenomenon_id bigint NOT NULL,
    identifier character varying(255) NOT NULL,
    fk_identifier_codespace_id bigint,
    name character varying(255),
    fk_name_codespace_id bigint,
    description character varying(255)
);
ALTER TABLE public.phenomenon OWNER TO postgres;
CREATE TABLE public.phenomenon_i18n (
    phenomenon_i18n_id bigint NOT NULL,
    fk_phenomenon_id bigint NOT NULL,
    locale character varying(255),
    name character varying(255),
    description character varying(255)
);
ALTER TABLE public.phenomenon_i18n OWNER TO postgres;
CREATE TABLE public.procedure (
    procedure_id bigint NOT NULL,
    identifier character varying(255),
    name character varying(255),
    is_mobile smallint,
    is_insitu smallint,
    fk_identifier_codespace_id bigint,
    fk_name_codespace_id bigint,
    description character varying(255),
    is_deleted smallint DEFAULT 0 NOT NULL,
    description_file text,
    is_reference smallint DEFAULT 0,
    fk_type_of_procedure_id bigint,
    is_aggregation smallint DEFAULT 1,
    fk_format_id bigint NOT NULL,
    CONSTRAINT procedure_is_aggregation_check CHECK ((is_aggregation = ANY (ARRAY[1, 0]))),
    CONSTRAINT procedure_is_deleted_check CHECK ((is_deleted = ANY (ARRAY[1, 0]))),
    CONSTRAINT procedure_is_reference_check CHECK ((is_reference = ANY (ARRAY[1, 0])))
);
ALTER TABLE public.procedure OWNER TO postgres;
CREATE TABLE public.procedure_hierarchy (
    fk_child_procedure_id bigint NOT NULL,
    fk_parent_procedure_id bigint NOT NULL
);
ALTER TABLE public.procedure_hierarchy OWNER TO postgres;
CREATE TABLE public.procedure_history (
    procedure_history_id bigint NOT NULL,
    fk_procedure_id bigint NOT NULL,
    fk_format_id bigint NOT NULL,
    valid_from timestamp without time zone NOT NULL,
    valid_to timestamp without time zone,
    xml text NOT NULL
);
ALTER TABLE public.procedure_history OWNER TO postgres;
CREATE TABLE public.procedure_i18n (
    procedure_i18n_id bigint NOT NULL,
    fk_procedure_id bigint NOT NULL,
    locale character varying(255),
    name character varying(255),
    description character varying(255),
    short_name character varying(255),
    long_name character varying(255)
);
ALTER TABLE public.procedure_i18n OWNER TO postgres;
CREATE TABLE public.related_dataset (
    fk_dataset_id bigint NOT NULL,
    fk_related_dataset_id bigint NOT NULL,
    role character varying(255),
    url character varying(255)
);
ALTER TABLE public.related_dataset OWNER TO postgres;
CREATE TABLE public.related_feature (
    related_feature_id bigint NOT NULL,
    fk_feature_id bigint NOT NULL,
    role character varying(255) NOT NULL
);
ALTER TABLE public.related_feature OWNER TO postgres;
CREATE TABLE public.related_observation (
    fk_observation_id bigint NOT NULL,
    fk_related_observation_id bigint NOT NULL,
    role character varying(255),
    url character varying(255)
);
ALTER TABLE public.related_observation OWNER TO postgres;
CREATE TABLE public.result_template (
    result_template_id bigint NOT NULL,
    fk_offering_id bigint NOT NULL,
    fk_phenomenon_id bigint NOT NULL,
    fk_procedure_id bigint,
    fk_feature_id bigint,
    identifier character varying(255) NOT NULL,
    structure text NOT NULL,
    encoding text NOT NULL
);
ALTER TABLE public.result_template OWNER TO postgres;
CREATE TABLE public.service (
    service_id bigint NOT NULL,
    name character varying(255) NOT NULL,
    description character varying(255),
    url character varying(255),
    type character varying(255),
    version character varying(255)
);
ALTER TABLE public.service OWNER TO postgres;
CREATE TABLE public.unit (
    unit_id bigint NOT NULL,
    symbol character varying(255) NOT NULL,
    name character varying(255),
    link character varying(255)
);
ALTER TABLE public.unit OWNER TO postgres;
CREATE TABLE public.unit_i18n (
    unit_i18n_id bigint NOT NULL,
    fk_unit_id bigint NOT NULL,
    locale character varying(255),
    name character varying(255)
);
ALTER TABLE public.unit_i18n OWNER TO postgres;
CREATE TABLE public.value_blob (
    fk_observation_id bigint NOT NULL,
    value oid
);
ALTER TABLE public.value_blob OWNER TO postgres;
COMMENT ON COLUMN public.value_blob.value IS 'Blob observation value';
CREATE TABLE public.value_data_array (
    fk_observation_id bigint NOT NULL,
    structure text NOT NULL,
    encoding text NOT NULL
);
ALTER TABLE public.value_data_array OWNER TO postgres;
CREATE TABLE public.value_profile (
    fk_observation_id bigint NOT NULL,
    vertical_from_name character varying(255),
    vertical_to_name character varying(255),
    fk_vertical_unit_id bigint NOT NULL
);
ALTER TABLE public.value_profile OWNER TO postgres;
ALTER TABLE ONLY public.category_i18
    ADD CONSTRAINT category_i18_pkey PRIMARY KEY (category_i18n_id);
ALTER TABLE ONLY public.category
    ADD CONSTRAINT category_pkey PRIMARY KEY (category_id);
ALTER TABLE ONLY public.codespace
    ADD CONSTRAINT codespace_pkey PRIMARY KEY (codespace_id);
ALTER TABLE ONLY public.composite_observation
    ADD CONSTRAINT composite_observation_pkey PRIMARY KEY (fk_parent_observation_id, fk_child_observation_id);
ALTER TABLE ONLY public.composite_phenomenon
    ADD CONSTRAINT composite_phenomenon_pkey PRIMARY KEY (fk_child_phenomenon_id, fk_parent_phenomenon_id);
ALTER TABLE ONLY public.dataset_parameter
    ADD CONSTRAINT dataset_parameter_pkey PRIMARY KEY (fk_dataset_id, fk_parameter_id);
ALTER TABLE ONLY public.dataset
    ADD CONSTRAINT dataset_pkey PRIMARY KEY (dataset_id);
ALTER TABLE ONLY public.dataset_reference
    ADD CONSTRAINT dataset_reference_pkey PRIMARY KEY (fk_dataset_id_from, sort_order);
ALTER TABLE ONLY public.feature_hierarchy
    ADD CONSTRAINT feature_hierarchy_pkey PRIMARY KEY (fk_child_feature_id, fk_parent_feature_id);
ALTER TABLE ONLY public.feature_i18n
    ADD CONSTRAINT feature_i18n_pkey PRIMARY KEY (feature_i18n_id);
ALTER TABLE ONLY public.feature_parameter
    ADD CONSTRAINT feature_parameter_pkey PRIMARY KEY (fk_feature_id, fk_parameter_id);
ALTER TABLE ONLY public.feature
    ADD CONSTRAINT feature_pkey PRIMARY KEY (feature_id);
ALTER TABLE ONLY public.phenomenon
    ADD CONSTRAINT fk_phenomenon_id UNIQUE (identifier);
ALTER TABLE ONLY public.format
    ADD CONSTRAINT format_pkey PRIMARY KEY (format_id);
ALTER TABLE ONLY public.observation
    ADD CONSTRAINT observation_pkey PRIMARY KEY (observation_id);
ALTER TABLE ONLY public.offering_feature_type
    ADD CONSTRAINT offering_feature_type_pkey PRIMARY KEY (fk_offering_id, fk_format_id);
ALTER TABLE ONLY public.offering_hierarchy
    ADD CONSTRAINT offering_hierarchy_pkey PRIMARY KEY (fk_child_offering_id, fk_parent_offering_id);
ALTER TABLE ONLY public.offering_i18n
    ADD CONSTRAINT offering_i18n_pkey PRIMARY KEY (offering_i18n_id);
ALTER TABLE ONLY public.offering_observation_type
    ADD CONSTRAINT offering_observation_type_pkey PRIMARY KEY (fk_offering_id, fk_format_id);
ALTER TABLE ONLY public.offering
    ADD CONSTRAINT offering_pkey PRIMARY KEY (offering_id);
ALTER TABLE ONLY public.offering_related_feature
    ADD CONSTRAINT offering_related_feature_pkey PRIMARY KEY (fk_offering_id, fk_related_feature_id);
ALTER TABLE ONLY public.parameter
    ADD CONSTRAINT parameter_pkey PRIMARY KEY (parameter_id);
ALTER TABLE ONLY public.phenomenon_i18n
    ADD CONSTRAINT phenomenon_i18n_pkey PRIMARY KEY (phenomenon_i18n_id);
ALTER TABLE ONLY public.phenomenon
    ADD CONSTRAINT phenomenon_pkey PRIMARY KEY (phenomenon_id);
ALTER TABLE ONLY public.procedure_hierarchy
    ADD CONSTRAINT procedure_hierarchy_pkey PRIMARY KEY (fk_parent_procedure_id, fk_child_procedure_id);
ALTER TABLE ONLY public.procedure_history
    ADD CONSTRAINT procedure_history_pkey PRIMARY KEY (procedure_history_id);
ALTER TABLE ONLY public.procedure_i18n
    ADD CONSTRAINT procedure_i18n_pkey PRIMARY KEY (procedure_i18n_id);
ALTER TABLE ONLY public.procedure
    ADD CONSTRAINT procedure_pkey PRIMARY KEY (procedure_id);
ALTER TABLE ONLY public.related_dataset
    ADD CONSTRAINT related_dataset_pkey PRIMARY KEY (fk_dataset_id, fk_related_dataset_id);
ALTER TABLE ONLY public.related_feature
    ADD CONSTRAINT related_feature_pkey PRIMARY KEY (related_feature_id);
ALTER TABLE ONLY public.related_observation
    ADD CONSTRAINT related_observation_pkey PRIMARY KEY (fk_observation_id, fk_related_observation_id);
ALTER TABLE ONLY public.result_template
    ADD CONSTRAINT result_template_pkey PRIMARY KEY (result_template_id);
ALTER TABLE ONLY public.service
    ADD CONSTRAINT service_pkey PRIMARY KEY (service_id);
ALTER TABLE ONLY public.codespace
    ADD CONSTRAINT un_codespace_codespace UNIQUE (name);
ALTER TABLE ONLY public.observation
    ADD CONSTRAINT un_data_identifier UNIQUE (identifier);
ALTER TABLE ONLY public.observation
    ADD CONSTRAINT un_data_identity UNIQUE (fk_dataset_id, sampling_time_start, sampling_time_end, result_time, vertical_from, vertical_to);
ALTER TABLE ONLY public.dataset
    ADD CONSTRAINT un_dataset_identifier UNIQUE (identifier);
ALTER TABLE ONLY public.dataset
    ADD CONSTRAINT un_dataset_identity UNIQUE (fk_procedure_id, fk_phenomenon_id, fk_offering_id, fk_category_id, fk_feature_id);
ALTER TABLE ONLY public.feature
    ADD CONSTRAINT un_feature_identifier UNIQUE (identifier);
ALTER TABLE ONLY public.feature
    ADD CONSTRAINT un_feature_url UNIQUE (url);
ALTER TABLE ONLY public.format
    ADD CONSTRAINT un_format_definition UNIQUE (definition);
ALTER TABLE ONLY public.offering
    ADD CONSTRAINT un_offering_identifier UNIQUE (identifier);
ALTER TABLE ONLY public.procedure
    ADD CONSTRAINT un_procedure_identifier UNIQUE (identifier);
ALTER TABLE ONLY public.service
    ADD CONSTRAINT un_service_name UNIQUE (name);
ALTER TABLE ONLY public.unit
    ADD CONSTRAINT un_unit_symbol UNIQUE (symbol);
ALTER TABLE ONLY public.unit_i18n
    ADD CONSTRAINT unit_i18n_pkey PRIMARY KEY (unit_i18n_id);
ALTER TABLE ONLY public.unit
    ADD CONSTRAINT unit_pkey PRIMARY KEY (unit_id);
ALTER TABLE ONLY public.value_blob
    ADD CONSTRAINT value_blob_pkey PRIMARY KEY (fk_observation_id);
ALTER TABLE ONLY public.value_data_array
    ADD CONSTRAINT value_data_array_pkey PRIMARY KEY (fk_observation_id);
ALTER TABLE ONLY public.value_profile
    ADD CONSTRAINT value_profile_pkey PRIMARY KEY (fk_observation_id);
CREATE INDEX idx_category_identifier ON public.category USING btree (identifier);
CREATE INDEX idx_dataset_identifier ON public.dataset USING btree (identifier);
CREATE INDEX idx_end_time ON public.procedure_history USING btree (valid_to);
CREATE INDEX idx_feature_identifier ON public.feature USING btree (identifier);
CREATE INDEX idx_offering_identifier ON public.offering USING btree (identifier);
CREATE INDEX idx_param_name ON public.parameter USING btree (name);
CREATE INDEX idx_phenomenon_identifier ON public.phenomenon USING btree (identifier);
CREATE INDEX idx_procedure_identifier ON public.procedure USING btree (identifier);
CREATE INDEX idx_result_template_identifier ON public.result_template USING btree (identifier);
CREATE INDEX idx_result_template_offering ON public.result_template USING btree (fk_offering_id);
CREATE INDEX idx_result_template_phenomenon ON public.result_template USING btree (fk_phenomenon_id);
CREATE INDEX idx_result_template_procedure ON public.result_template USING btree (fk_procedure_id);
CREATE INDEX idx_start_time ON public.procedure_history USING btree (valid_from);
CREATE INDEX related_observation_idx ON public.related_observation USING btree (fk_observation_id);
CREATE INDEX result_time_idx ON public.observation USING btree (result_time);
CREATE INDEX sampling_time_end_idx ON public.observation USING btree (sampling_time_end);
CREATE INDEX sampling_time_start_idx ON public.observation USING btree (sampling_time_start);
ALTER TABLE ONLY public.value_blob
    ADD CONSTRAINT fk_blob_value FOREIGN KEY (fk_observation_id) REFERENCES public.observation(observation_id);
ALTER TABLE ONLY public.category_i18
    ADD CONSTRAINT fk_category FOREIGN KEY (fk_category_id) REFERENCES public.category(category_id);
ALTER TABLE ONLY public.composite_observation
    ADD CONSTRAINT fk_composite_observation FOREIGN KEY (fk_parent_observation_id) REFERENCES public.observation(observation_id);
ALTER TABLE ONLY public.composite_observation
    ADD CONSTRAINT fk_composite_observation_child FOREIGN KEY (fk_child_observation_id) REFERENCES public.observation(observation_id);
ALTER TABLE ONLY public.value_data_array
    ADD CONSTRAINT fk_data_array_value FOREIGN KEY (fk_observation_id) REFERENCES public.observation(observation_id);
ALTER TABLE ONLY public.observation
    ADD CONSTRAINT fk_data_identifier_codespace FOREIGN KEY (fk_identifier_codespace_id) REFERENCES public.codespace(codespace_id);
ALTER TABLE ONLY public.observation
    ADD CONSTRAINT fk_data_name_codespace FOREIGN KEY (fk_name_codespace_id) REFERENCES public.codespace(codespace_id);
ALTER TABLE ONLY public.dataset
    ADD CONSTRAINT fk_dataset_category FOREIGN KEY (fk_category_id) REFERENCES public.category(category_id);
ALTER TABLE ONLY public.dataset
    ADD CONSTRAINT fk_dataset_codespace FOREIGN KEY (fk_identifier_codespace_id) REFERENCES public.codespace(codespace_id);
ALTER TABLE ONLY public.dataset
    ADD CONSTRAINT fk_dataset_feature FOREIGN KEY (fk_feature_id) REFERENCES public.feature(feature_id);
ALTER TABLE ONLY public.dataset
    ADD CONSTRAINT fk_dataset_first_obs FOREIGN KEY (fk_first_observation_id) REFERENCES public.observation(observation_id);
ALTER TABLE ONLY public.dataset
    ADD CONSTRAINT fk_dataset_last_obs FOREIGN KEY (fk_last_observation_id) REFERENCES public.observation(observation_id);
ALTER TABLE ONLY public.dataset
    ADD CONSTRAINT fk_dataset_name_codespace FOREIGN KEY (fk_name_codespace_id) REFERENCES public.codespace(codespace_id);
ALTER TABLE ONLY public.dataset
    ADD CONSTRAINT fk_dataset_observation_type FOREIGN KEY (fk_format_id) REFERENCES public.format(format_id);
ALTER TABLE ONLY public.dataset
    ADD CONSTRAINT fk_dataset_offering FOREIGN KEY (fk_offering_id) REFERENCES public.offering(offering_id);
ALTER TABLE ONLY public.dataset_parameter
    ADD CONSTRAINT fk_dataset_parameter FOREIGN KEY (fk_dataset_id) REFERENCES public.dataset(dataset_id);
ALTER TABLE ONLY public.dataset
    ADD CONSTRAINT fk_dataset_phenomenon FOREIGN KEY (fk_phenomenon_id) REFERENCES public.phenomenon(phenomenon_id);
ALTER TABLE ONLY public.dataset
    ADD CONSTRAINT fk_dataset_procedure FOREIGN KEY (fk_procedure_id) REFERENCES public.procedure(procedure_id);
ALTER TABLE ONLY public.dataset_reference
    ADD CONSTRAINT fk_dataset_reference_from FOREIGN KEY (fk_dataset_id_from) REFERENCES public.dataset(dataset_id);
ALTER TABLE ONLY public.dataset_reference
    ADD CONSTRAINT fk_dataset_reference_to FOREIGN KEY (fk_dataset_id_to) REFERENCES public.dataset(dataset_id);
ALTER TABLE ONLY public.dataset
    ADD CONSTRAINT fk_dataset_unit FOREIGN KEY (fk_unit_id) REFERENCES public.unit(unit_id);
ALTER TABLE ONLY public.feature_i18n
    ADD CONSTRAINT fk_feature FOREIGN KEY (fk_feature_id) REFERENCES public.feature(feature_id);
ALTER TABLE ONLY public.feature_hierarchy
    ADD CONSTRAINT fk_feature_child FOREIGN KEY (fk_child_feature_id) REFERENCES public.feature(feature_id);
ALTER TABLE ONLY public.feature
    ADD CONSTRAINT fk_feature_format FOREIGN KEY (fk_format_id) REFERENCES public.format(format_id);
ALTER TABLE ONLY public.feature
    ADD CONSTRAINT fk_feature_identifier_codespace FOREIGN KEY (fk_identifier_codespace_id) REFERENCES public.codespace(codespace_id);
ALTER TABLE ONLY public.feature
    ADD CONSTRAINT fk_feature_name_codespace FOREIGN KEY (fk_name_codespace_id) REFERENCES public.codespace(codespace_id);
ALTER TABLE ONLY public.feature_parameter
    ADD CONSTRAINT fk_feature_parameter FOREIGN KEY (fk_feature_id) REFERENCES public.feature(feature_id);
ALTER TABLE ONLY public.feature_hierarchy
    ADD CONSTRAINT fk_feature_parent FOREIGN KEY (fk_parent_feature_id) REFERENCES public.feature(feature_id);
ALTER TABLE ONLY public.offering_feature_type
    ADD CONSTRAINT fk_feature_type_offering FOREIGN KEY (fk_format_id) REFERENCES public.format(format_id);
ALTER TABLE ONLY public.procedure_i18n
    ADD CONSTRAINT fk_i18n_procedure FOREIGN KEY (fk_procedure_id) REFERENCES public.procedure(procedure_id);
ALTER TABLE ONLY public.observation_parameter
    ADD CONSTRAINT fk_observation_parameter FOREIGN KEY (fk_observation_id) REFERENCES public.observation(observation_id);
ALTER TABLE ONLY public.offering_observation_type
    ADD CONSTRAINT fk_observation_type_offering FOREIGN KEY (fk_format_id) REFERENCES public.format(format_id);
ALTER TABLE ONLY public.offering_i18n
    ADD CONSTRAINT fk_offering FOREIGN KEY (fk_offering_id) REFERENCES public.offering(offering_id);
ALTER TABLE ONLY public.offering_hierarchy
    ADD CONSTRAINT fk_offering_child FOREIGN KEY (fk_child_offering_id) REFERENCES public.offering(offering_id);
ALTER TABLE ONLY public.offering_feature_type
    ADD CONSTRAINT fk_offering_feature_type FOREIGN KEY (fk_offering_id) REFERENCES public.offering(offering_id);
ALTER TABLE ONLY public.offering
    ADD CONSTRAINT fk_offering_identifier_codespace FOREIGN KEY (fk_identifier_codespace_id) REFERENCES public.codespace(codespace_id);
ALTER TABLE ONLY public.offering
    ADD CONSTRAINT fk_offering_name_codespace FOREIGN KEY (fk_name_codespace_id) REFERENCES public.codespace(codespace_id);
ALTER TABLE ONLY public.offering_observation_type
    ADD CONSTRAINT fk_offering_observation_type FOREIGN KEY (fk_offering_id) REFERENCES public.offering(offering_id);
ALTER TABLE ONLY public.offering_hierarchy
    ADD CONSTRAINT fk_offering_parent FOREIGN KEY (fk_parent_offering_id) REFERENCES public.offering(offering_id);
ALTER TABLE ONLY public.offering_related_feature
    ADD CONSTRAINT fk_offering_related_feature FOREIGN KEY (fk_related_feature_id) REFERENCES public.related_feature(related_feature_id);
ALTER TABLE ONLY public.parameter
    ADD CONSTRAINT fk_param_unit FOREIGN KEY (fk_unit_id) REFERENCES public.unit(unit_id);
ALTER TABLE ONLY public.dataset_parameter
    ADD CONSTRAINT fk_parameter_dataset FOREIGN KEY (fk_parameter_id) REFERENCES public.parameter(parameter_id);
ALTER TABLE ONLY public.feature_parameter
    ADD CONSTRAINT fk_parameter_feature FOREIGN KEY (fk_parameter_id) REFERENCES public.parameter(parameter_id);
ALTER TABLE ONLY public.observation_parameter
    ADD CONSTRAINT fk_parameter_observation FOREIGN KEY (fk_parameter_id) REFERENCES public.parameter(parameter_id);
ALTER TABLE ONLY public.procedure_history
    ADD CONSTRAINT fk_pdf_id FOREIGN KEY (fk_format_id) REFERENCES public.format(format_id);
ALTER TABLE ONLY public.phenomenon_i18n
    ADD CONSTRAINT fk_phenomenon FOREIGN KEY (fk_phenomenon_id) REFERENCES public.phenomenon(phenomenon_id);
ALTER TABLE ONLY public.composite_phenomenon
    ADD CONSTRAINT fk_phenomenon_child FOREIGN KEY (fk_child_phenomenon_id) REFERENCES public.phenomenon(phenomenon_id);
ALTER TABLE ONLY public.phenomenon
    ADD CONSTRAINT fk_phenomenon_identifier_codespace FOREIGN KEY (fk_identifier_codespace_id) REFERENCES public.codespace(codespace_id);
ALTER TABLE ONLY public.phenomenon
    ADD CONSTRAINT fk_phenomenon_name_codespace FOREIGN KEY (fk_name_codespace_id) REFERENCES public.codespace(codespace_id);
ALTER TABLE ONLY public.composite_phenomenon
    ADD CONSTRAINT fk_phenomenon_parent FOREIGN KEY (fk_parent_phenomenon_id) REFERENCES public.phenomenon(phenomenon_id);
ALTER TABLE ONLY public.procedure_hierarchy
    ADD CONSTRAINT fk_procedure_child FOREIGN KEY (fk_child_procedure_id) REFERENCES public.procedure(procedure_id);
ALTER TABLE ONLY public.procedure
    ADD CONSTRAINT fk_procedure_format FOREIGN KEY (fk_format_id) REFERENCES public.format(format_id);
ALTER TABLE ONLY public.procedure_history
    ADD CONSTRAINT fk_procedure_history FOREIGN KEY (fk_procedure_id) REFERENCES public.procedure(procedure_id);
ALTER TABLE ONLY public.procedure
    ADD CONSTRAINT fk_procedure_identifier_codespace FOREIGN KEY (fk_identifier_codespace_id) REFERENCES public.codespace(codespace_id);
ALTER TABLE ONLY public.procedure
    ADD CONSTRAINT fk_procedure_name_codespace FOREIGN KEY (fk_name_codespace_id) REFERENCES public.codespace(codespace_id);
ALTER TABLE ONLY public.procedure_hierarchy
    ADD CONSTRAINT fk_procedure_parent FOREIGN KEY (fk_parent_procedure_id) REFERENCES public.procedure(procedure_id);
ALTER TABLE ONLY public.value_profile
    ADD CONSTRAINT fk_profile_unit FOREIGN KEY (fk_vertical_unit_id) REFERENCES public.unit(unit_id);
ALTER TABLE ONLY public.value_profile
    ADD CONSTRAINT fk_profile_value FOREIGN KEY (fk_observation_id) REFERENCES public.observation(observation_id);
ALTER TABLE ONLY public.related_dataset
    ADD CONSTRAINT fk_rel_dataset_dataset FOREIGN KEY (fk_dataset_id) REFERENCES public.dataset(dataset_id);
ALTER TABLE ONLY public.related_dataset
    ADD CONSTRAINT fk_rel_dataset_rel_dataset FOREIGN KEY (fk_related_dataset_id) REFERENCES public.dataset(dataset_id);
ALTER TABLE ONLY public.related_observation
    ADD CONSTRAINT fk_rel_obs_related FOREIGN KEY (fk_related_observation_id) REFERENCES public.observation(observation_id);
ALTER TABLE ONLY public.related_feature
    ADD CONSTRAINT fk_related_feature FOREIGN KEY (fk_feature_id) REFERENCES public.feature(feature_id);
ALTER TABLE ONLY public.offering_related_feature
    ADD CONSTRAINT fk_related_feature_offering FOREIGN KEY (fk_offering_id) REFERENCES public.offering(offering_id);
ALTER TABLE ONLY public.related_observation
    ADD CONSTRAINT fk_related_observation FOREIGN KEY (fk_observation_id) REFERENCES public.observation(observation_id);
ALTER TABLE ONLY public.result_template
    ADD CONSTRAINT fk_result_template_feature FOREIGN KEY (fk_feature_id) REFERENCES public.feature(feature_id);
ALTER TABLE ONLY public.result_template
    ADD CONSTRAINT fk_result_template_offering FOREIGN KEY (fk_offering_id) REFERENCES public.offering(offering_id);
ALTER TABLE ONLY public.result_template
    ADD CONSTRAINT fk_result_template_phenomenon FOREIGN KEY (fk_phenomenon_id) REFERENCES public.phenomenon(phenomenon_id);
ALTER TABLE ONLY public.result_template
    ADD CONSTRAINT fk_result_template_procedure FOREIGN KEY (fk_procedure_id) REFERENCES public.procedure(procedure_id);
ALTER TABLE ONLY public.observation
    ADD CONSTRAINT fk_sampling_geom_dataset FOREIGN KEY (fk_dataset_id) REFERENCES public.dataset(dataset_id);
ALTER TABLE ONLY public.procedure
    ADD CONSTRAINT fk_type_of FOREIGN KEY (fk_type_of_procedure_id) REFERENCES public.procedure(procedure_id);
ALTER TABLE ONLY public.unit_i18n
    ADD CONSTRAINT fk_unit FOREIGN KEY (fk_unit_id) REFERENCES public.unit(unit_id);
create sequence public.category_i18n_seq start 1 increment 1;
create sequence public.category_seq start 1 increment 1;
create sequence public.codespace_seq start 1 increment 1;
create sequence public.dataset_seq start 1 increment 1;
create sequence public.feature_i18n_seq start 1 increment 1;
create sequence public.feature_seq start 1 increment 1;
create sequence public.format_seq start 1 increment 1;
create sequence public.observation_seq start 1 increment 1;
create sequence public.offering_i18n_seq start 1 increment 1;
create sequence public.offering_seq start 1 increment 1;
create sequence public.parameter_seq start 1 increment 1;
create sequence public.phenomenon_i18n_seq start 1 increment 1;
create sequence public.phenomenon_seq start 1 increment 1;
create sequence public.procedure_history_seq start 1 increment 1;
create sequence public.procedure_i18n_seq start 1 increment 1;
create sequence public.procedure_seq start 1 increment 1;
create sequence public.related_feature_seq start 1 increment 1;
create sequence public.result_template_seq start 1 increment 1;
create sequence public.service_seq start 1 increment 1;
create sequence public.unit_i18n_seq start 1 increment 1;
create sequence public.unit_seq start 1 increment 1;


