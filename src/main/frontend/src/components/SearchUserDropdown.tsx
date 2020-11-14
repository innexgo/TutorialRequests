import React from 'react';

import AsyncSelect from 'react-select/async';
import { viewUser, isApiErrorCode } from '../utils/utils';

interface SearchUserDropdownProps {
  invalid: boolean,
  apiKey: ApiKey,
  userKind: "STUDENT" | "USER" | "ADMIN",
  setFn: (id: number | null) => void
}

type UserOption = {
  label: string,
  value: number
}

export default function SearchUserDropdown(props: SearchUserDropdownProps) {
  const promiseOptions = async function(input: string): Promise<UserOption[]> {
    const results = await viewUser({
      partialUserName: input.toUpperCase(),
      userKind: props.userKind,
      apiKey: props.apiKey.key
    });

    if (isApiErrorCode(results)) {
      return [];
    }

    return results.map((x: User): UserOption => ({
      label: `${x.name} -- ${x.email}`,
      value: x.id
    }));
  };


  const onChange = (opt: any) => {
    if (opt == null) {
      props.setFn(null);
    } else {
      props.setFn((opt as UserOption).value)
    }
  }

  return <AsyncSelect
    placeholder="Start typing to search"
    isClearable={true}
    onChange={onChange}
    cacheOptions={true}
    isInvalid={props.invalid}
    noOptionsMessage={() => null}
    loadOptions={promiseOptions} />
}
