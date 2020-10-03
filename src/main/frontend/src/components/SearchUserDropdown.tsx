import React from 'react';

import AsyncSelect from 'react-select/async';
import { fetchApi } from '../utils/utils';

interface SearchUserDropdownProps {
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
    const results = await fetchApi('user/?' + new URLSearchParams([
      ['partialUserName', `${input.toUpperCase()}`],
      ['userKind', props.userKind],
      ['apiKey', `${props.apiKey.key}`],
    ])) as User[];
    return results.map((x:User) => {
      return {
        label: `${x.name} -- ${x.email}`,
        value: x.id
      } as UserOption
    });
  };

  const onChange = (opt:any) => {
    if(opt == null) {
      props.setFn(null);
    } else {
      props.setFn((opt as UserOption).value)
    }
  }

  return <AsyncSelect isClearable={true} onChange={onChange} loadOptions={promiseOptions} />
}
